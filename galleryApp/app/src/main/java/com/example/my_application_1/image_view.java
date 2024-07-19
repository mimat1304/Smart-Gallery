package com.example.my_application_1;

import static java.lang.Double.NaN;
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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileOutputStream;
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
    List<Integer> userIDs=new ArrayList<>();
    List<Integer> faceIds = new ArrayList<>();
    List<String> globalNames=new ArrayList<>();
    boolean isDetected=false;
    List<String>users;
    float[] zeroEmbeddings=new float[512];
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
        users = intent.getStringArrayListExtra("users");

        ImageView imageView = findViewById(R.id.imageView);
        try {
            bitmap = handleSamplingAndRotationBitmap(filePath);
            bitmapToShow=bitmap;

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
        List<String>names= new ArrayList<>();
        setContentView(R.layout.activity_identify_faces);
        Context context=this;
        executorService.submit(() -> {
            faceIds = db.faceDao().getAllFaceIds(filePath);
            userIDs = db.faceDao().getAllUsers(filePath);

            for (int userID : userIDs) {
                names.add((db.userDao().findByUID(userID)).name);
            }
            globalNames=names;
            ListView listView= findViewById((R.id.detected_faces_list));
            List<identification_variables> items=new ArrayList<>();
            for(int i=0;i<min(croppedFaces.size(),names.size());i++){
                items.add(new identification_variables(croppedFaces.get(i), names.get(i)));
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    identify_face_adapter adapter =new identify_face_adapter(context,R.layout.face_identification,items,users);
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
                AutoCompleteTextView autoCompleteTextView = item.findViewById(R.id.name);
                EditText editName= item.findViewById(R.id.editText);
                String text = autoCompleteTextView.getText().toString();
                final int index=i;
                if(!editName.getText().toString().equals("")){
                    executorService.submit(()->{
                        User user = db.userDao().findByUID(userIDs.get(index));
                        user.name=editName.getText().toString();
                        db.userDao().updateUser(user);
                    });
                    break;
                }
                if(globalNames.get(i).equals("Unknown")) {
                    if (text.length() != 0) {
                        int j = i;
                        executorService.submit(() -> {
                            List<User> users_new = db.userDao().findByName(text);
                            if(users_new.size() == 0) {
                                User user = db.userDao().findByUID(userIDs.get(index));
                                user.name = text;
                                db.userDao().updateUser(user);
                            }else{
                                try {
                                    showDialog(users_new,null,j);
                                } catch (IOException e){
                                    Log.e("dialog","error",e);
                                }
                            }
                        });
                    }
                }else{
                    if (text.length() != 0) {
                        int j = i;
                        executorService.submit(() -> {
                            User user_old = db.userDao().findByUID(userIDs.get(index));
                            List<User> users_new = db.userDao().findByName(text);

                            if(users_new.size()==0){
                                int N = db.userDao().getUserListSize();
                                User user_new = new User(text, 1, embeddings[j]);
                                user_new.uid = N+1;
                                db.userDao().insert(user_new);
                                Face face =db.faceDao().getFaceFromUid(faceIds.get(j));
                                face.userID = N+1;
                                db.faceDao().updateFace(face);
                                continueSaveOperation(user_old,user_new,j);
                            }else{
                                try {
                                    showDialog(users_new,user_old,j);
                                } catch (IOException e){
                                    Log.e("dialog","error",e);
                                }
                            }

                        });
                    }
                }
            }
        }
        onResume();
    }
    private void showDialog(List<User> users_new,User user_old,int j) throws IOException {
        // Inflate the dialog layout
        List<Item> items = new ArrayList<>();
        String text= users_new.get(0).name;
        for(int i=0;i<users_new.size();i++) {
            User user = users_new.get(i);
            if (user.n == 0) {
                continue;
            }
            Face face = db.faceDao().loadAllByUserIds(user.uid).get(0);
            float[] coordinates = face.coordinates;
            float rollAngle = face.rollAngle;
            String filePath = face.filePath;
            Bitmap bitmap = handleSamplingAndRotationBitmap(filePath);
            Matrix matrix = new Matrix();
            matrix.setRotate(rollAngle, (coordinates[0] + coordinates[2]) / 2, (coordinates[1] + coordinates[3]) / 2);
            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());

            Canvas canvas = new Canvas(rotatedBitmap);
            canvas.drawBitmap(bitmap, matrix, new Paint());

            int left = Math.max(0, (int) coordinates[0]);
            int top = Math.max(0, (int) coordinates[1]);
            int right = Math.min(rotatedBitmap.getWidth(), (int) coordinates[2]);
            int bottom = Math.min(rotatedBitmap.getHeight(), (int) coordinates[3]);

            bitmap = Bitmap.createBitmap(rotatedBitmap, left, top, right - left, bottom - top);
            items.add(new Item(bitmap, user.name));
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.dialog_pop_up, null);

                // Create the dialog builder
                AlertDialog.Builder builder = new AlertDialog.Builder(image_view.this);
                builder.setView(dialogView);
                ListView listView = dialogView.findViewById(R.id.dialog_list_view);
                DialogCustomAdapter adapter = new DialogCustomAdapter(image_view.this, items);
                listView.setAdapter(adapter);
                AlertDialog dialog = builder.create();
                dialog.show();

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        executorService.submit(()->{
                            User user_new = users_new.get(position);
                            user_new.embeddings=meanEmbeddings(embeddings[j], user_new.embeddings, user_new.n);
                            user_new.n=user_new.n+1;
                            db.userDao().updateUser(user_new);
                            Face face =db.faceDao().getFaceFromUid(faceIds.get(j));
                            face.userID = user_new.uid;
                            db.faceDao().updateFace(face);
                            continueSaveOperation(user_old,user_new,j);
                        });

                        Toast.makeText(image_view.this, "Selected position: " + position, Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
                Button dialogButton = dialogView.findViewById(R.id.dialog_button);
                dialogButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(image_view.this, "None", Toast.LENGTH_SHORT).show();
                        executorService.submit(()->{
                            User user = db.userDao().findByUID(userIDs.get(j));
                            user.name = text;
                            db.userDao().updateUser(user);
                        });
                        dialog.dismiss();
                    }
                });
            }
        });


    }

    void continueSaveOperation(User user_old, User user_new,int j){
        if(user_old == null){
            return;
        }
        if(user_old.n-1==0){
            user_old.embeddings = zeroEmbeddings;
            float similarity_with_new=similarity(user_new.embeddings,embeddings[j]);
            writeLogToCSV(filePath,faceIds.get(j),NaN,similarity_with_new);
        }else {
            user_old.embeddings = subMeanEmbeddings(embeddings[j], user_old.embeddings, user_old.n);
            user_old.n = user_old.n - 1;
            db.userDao().updateUser(user_old);
            float similarity_with_old = similarity(user_old.embeddings, embeddings[j]);
            float similarity_with_new = similarity(user_new.embeddings, embeddings[j]);
            writeLogToCSV(filePath, faceIds.get(j), similarity_with_old, similarity_with_new);
        }
        List<Face> faces=db.faceDao().loadAllByUserIds(user_old.uid);
        checkFaces(faces,user_old,user_new);
    }
    void checkFaces(List<Face>faces,User user_old,User user_new){
        float[] oldEM=user_old.embeddings;
        float[] newEM=user_new.embeddings;
        int old_n=user_old.n;
        int new_n=user_new.n;
        for(Face face:faces){
            float[] cur_em = face.embeddings;
            float[] newOldEM = subMeanEmbeddings(cur_em,oldEM,old_n);
            if((old_n-1==0 && similarity(newEM,cur_em)>=0.63) || (old_n-1>0 && similarity(newOldEM,cur_em)<similarity(newEM,cur_em))){
                newEM=meanEmbeddings(cur_em,newEM,new_n);
                user_new.n = ++new_n;
                user_old.n = --old_n;
                user_new.embeddings = newEM;
                if(old_n==0){
                    user_old.embeddings = zeroEmbeddings;
                }else {
                    user_old.embeddings = newOldEM;
                }
                face.userID=user_new.uid;
                db.faceDao().updateFace(face);
                db.userDao().updateUser(user_new);
                db.userDao().updateUser(user_old);

                oldEM = newOldEM;
                Log.d("checkFaces","User updated for face "+face.uid);
            }
            else{
                Log.d("checkFaces","User not updated for face "+face.uid);
            }
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(image_view.this, "Saved", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void writeLogToCSV(String filePath,int faceId, double similarity1, double similarity2) {
        Face face = db.faceDao().getFaceFromUid(faceId);
        String FILE_NAME="logs.csv";
        File csvFile = new File(getFilesDir(), FILE_NAME);
        boolean flag=false;
        if(!csvFile.exists()) {
            flag = true;
        }
        String csvRow = filePath + "," + faceId + "," + face.rollAngle + "," + face.coordinates[0] + "," + face.coordinates[1] + "," + face.coordinates[2] + "," + face.coordinates[3] + "," + similarity1 + "," + similarity2;
        try (FileOutputStream fos = openFileOutput(FILE_NAME, MODE_APPEND)) {
            if (flag) {
                String headers = "FilePath,FaceId,Z Angle,Left,Top,Right,Bottom,Similarity with previous cluster,Similarity with new cluster";
                fos.write((headers + "\n").getBytes());
            }
            fos.write((csvRow + "\n").getBytes());
        } catch (IOException e) {
            Log.e("image_view", "Error writing to CSV file", e);
        }
    }
    private float similarity(float[] em1, float[] em2){
        float ans=0.0f;
        try{
            float dotProduct = 0.0f;
            for (int i = 0; i < em1.length; i++) {
                dotProduct += em1[i] * em2[i];
            }
            float magnitude1 = 0.0f, magnitude2 = 0.0f;
            for (int i = 0; i < em1.length; i++) {
                magnitude1 += (em1[i] * em1[i]);
                magnitude2 += (em2[i] * em2[i]);
            }
            magnitude1 = (float) Math.sqrt(magnitude1);
            magnitude2 = (float) Math.sqrt(magnitude2);
            ans=dotProduct / (magnitude1 * magnitude2);
        }catch (Exception e){
            Log.e("Similarity","error",e);
        }
        return ans;
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
        faceDetectionActivity.processImage(this,bitmap);
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
        executorService.shutdown();
    }
}