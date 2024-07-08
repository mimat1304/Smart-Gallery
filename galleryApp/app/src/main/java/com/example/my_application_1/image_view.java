package com.example.my_application_1;

import static java.lang.Double.min;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class image_view extends AppCompatActivity {
    Bitmap bitmap = null;
    Bitmap bitmapToShow = null;
    public String filePath;
    public ArrayList<Rect>rects= new ArrayList<>();
    private List<Float>rollAngles= new ArrayList<>();
    private ArrayList<Bitmap> croppedFaces= new ArrayList<>();
    private float[][] embeddings;

    AppDatabase db;
    ExecutorService executorService;
    List<Integer>userIDs=new ArrayList<>();
    List<String>names= new ArrayList<>();
    List<Integer> faceIds = new ArrayList<>();
    boolean isDetected=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_image_view);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        db=AppDatabase.getInstance(getApplicationContext());
        executorService=Executors.newSingleThreadExecutor();
        Intent intent = getIntent();
        filePath = intent.getStringExtra("filePath");
        ImageView imageView = findViewById(R.id.imageView);
        Bitmap img= BitmapFactory.decodeFile(filePath);
        try {
            bitmap = rotateImageIfRequired(img, filePath);
            bitmapToShow=handleSamplingAndRotationBitmap(filePath);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        imageView.setImageBitmap(bitmapToShow);
        Button identifyFaces= findViewById(R.id.identificationButton);
        identifyFaces.setVisibility(View.INVISIBLE);
        faceDetection();
    }
    public static Bitmap handleSamplingAndRotationBitmap(String imagePath) throws IOException {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);

        options.inSampleSize = calculateInSampleSize(options, 1024, 1024);

        options.inJustDecodeBounds = false;
        Bitmap img = BitmapFactory.decodeFile(imagePath, options);

        return rotateImageIfRequired(img, imagePath);
    }
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private static Bitmap rotateImageIfRequired(Bitmap img, String imagePath) throws IOException {
        ExifInterface ei = new ExifInterface(imagePath);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    private static Bitmap rotateImage(Bitmap img, float degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }
    @Override
    protected void onResume() {
        super.onResume();
        setContentView(R.layout.activity_image_view);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        ImageView imageView = findViewById(R.id.imageView);
        imageView.setImageBitmap(bitmapToShow);
        Button identifyFaces= findViewById(R.id.identificationButton);
        identifyFaces.setVisibility(View.INVISIBLE);
        if(isDetected){
            identifyFaces.setVisibility(View.VISIBLE);
        }
    }
    public void identify_faces(View v){
        if(croppedFaces.size()==0){
            Toast.makeText(this, "No faces detected", Toast.LENGTH_SHORT).show();
            return;
        }
        setContentView(R.layout.activity_identify_faces);
        Context context=this;
        executorService.submit(() -> {
            faceIds = db.faceDao().getAllFaceIds(filePath);
            userIDs = db.faceDao().getAllUsers(filePath);
            for (int userID : userIDs) {
                names.add((db.userDao().findByUID(userID)).name);
            }
            ListView listView= findViewById((R.id.detected_faces_list));
            List<identification_variables> items=new ArrayList<>();
            for(int i=0;i<min(croppedFaces.size(),names.size());i++){
                items.add(new identification_variables(croppedFaces.get(i), names.get(i)));
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    identify_face_adapter adapter = new identify_face_adapter(context, R.layout.face_identification, items);
                    listView.setAdapter(adapter);
                }
            });
        });
    }
    public void onSaveClick(View v){
        ListView listView = findViewById(R.id.detected_faces_list);
        identify_face_adapter adapter = (identify_face_adapter) listView.getAdapter();

        for (int i = 0; i < adapter.getCount(); i++) {
            View item = listView.getChildAt(i);
            if (item != null) {
                EditText editText = item.findViewById(R.id.name);
                String text = editText.getText().toString();
                final int index=i;
                if(names.get(i).equals("Unknown")) {
                    if ((!(text.equals(names.get(i))) && (text.length() != 0))) {
                        executorService.submit(() -> {
                            User user = db.userDao().findByUID(userIDs.get(index));
                            user.name = text;
                            db.userDao().updateUser(user);
                        });
                    }
                }else{
                    if ((!(text.equals(names.get(i))) && (text.length() != 0))) {
                        int j = i;
                        executorService.submit(() -> {
                            User user_old = db.userDao().findByUID(userIDs.get(index));
                            User user_new = db.userDao().findByName(text);
                            if(user_new == null){
                                int N = db.userDao().getUserListSize();
                                user_new = new User(text, 1, embeddings[j]);
                                user_new.uid = N+1;
                                db.userDao().insert(user_new);
                                Face face =db.faceDao().getFaceFromUid(faceIds.get(j));
                                face.userID = N+1;
                                db.faceDao().updateFace(face);
                            }else{
                                user_new.embeddings=meanEmbeddings(user_new.embeddings, embeddings[j], user_new.n);
                                user_new.n=user_new.n+1;
                                db.userDao().updateUser(user_new);
                                Face face =db.faceDao().getFaceFromUid(faceIds.get(j));
                                face.userID = user_new.uid;
                                db.faceDao().updateFace(face);
                            }
                            user_old.embeddings = subMeanEmbeddings(user_old.embeddings, embeddings[j], user_old.n);
                            user_old.n = user_old.n-1;
                            db.userDao().updateUser(user_old);
                        });
                    }
                }
            }
        }
        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        onResume();
    }
    private float[] subMeanEmbeddings(float[] cur_em,float[] em,int n){
        float[] meanEM= new float[em.length];
        for (int i=0;i<em.length;i++){
            meanEM[i]=(em[i]*n-cur_em[i])/(n-1);
        }
        return meanEM;
    }

    private float[] meanEmbeddings(float[] cur_em,float[] em,int n){
        float[] meanEM= new float[em.length];
        for (int i=0;i<em.length;i++){
            meanEM[i]=(em[i]*n+cur_em[i])/(n+1);
        }
        return meanEM;
    }
    public void shareImage(View v) {
        File file = new File(filePath);
        Uri uri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", file);
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "Share Image"));
    }

    protected void faceDetection(){
        FaceDetectionCallback callback= new FaceDetectionCallback() {
            @Override
            public void onFacesDetected(ArrayList<Rect> faces, List<Float>RollAngles) {
                if(faces.size()==0){
                    return;
                }
                isDetected=true;
                Button identifyFaces= findViewById(R.id.identificationButton);
                identifyFaces.setVisibility(View.VISIBLE);
                rollAngles=RollAngles;
                rects=faces;
                crop_faces();
                extractEmbeddings();
            }

            @Override
            public void onFaceDetectionFailed(Exception e) {

            }
        };
        FaceDetection_Activity faceDetectionActivity= new FaceDetection_Activity();
        faceDetectionActivity.processImage(this,filePath);
        faceDetectionActivity.detectFaces(callback);

    }

    protected void crop_faces() {
        croppedFaces = new ArrayList<>();
        for (int i=0;i<rollAngles.size();i++) {
            float rollAngle=rollAngles.get(i);
            Rect faceRect= rects.get(i);

            Matrix matrix = new Matrix();
            matrix.setRotate(rollAngle, faceRect.centerX(), faceRect.centerY());

            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());

            Canvas canvas = new Canvas(rotatedBitmap);
            canvas.drawBitmap(bitmap, matrix, new Paint());

            int left = Math.max(0, faceRect.left);
            int top = Math.max(0, faceRect.top);
            int right = Math.min(rotatedBitmap.getWidth(), faceRect.right);
            int bottom = Math.min(rotatedBitmap.getHeight(), faceRect.bottom);

            croppedFaces.add( Bitmap.createBitmap(rotatedBitmap, left, top, right - left, bottom - top));
        }
    }
    protected void extractEmbeddings(){
        try {
            FaceNetEmbeddings faceNetEmbeddings = new FaceNetEmbeddings(this,"facenet.tflite");
            embeddings = faceNetEmbeddings.getEmbeddings(croppedFaces);
            Log.d("Embeddings status:", "Generated");
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
    public void showEmbeddings(View v){
        setContentView(R.layout.activity_show_embeddings);
        List<embeddings_list_item> items=new ArrayList<>();
        for(int i=0;i<croppedFaces.size();i++){
            items.add(new embeddings_list_item(croppedFaces.get(i), Arrays.toString(embeddings[i])));
        }
        ListView listView= findViewById((R.id.face_list_view_1));
        FacesAdapter adapter =new FacesAdapter(this,R.layout.cropped_faces,items);
        listView.setAdapter(adapter);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}