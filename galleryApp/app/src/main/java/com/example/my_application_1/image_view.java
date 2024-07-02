package com.example.my_application_1;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
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


    public String filePath;
    public ArrayList<Rect>rects;
    private ArrayList<Bitmap> croppedFaces;
    private float[][] embeddings;

    AppDatabase db;
    ExecutorService executorService;
    List<Integer>userIDs;
    List<String>names= new ArrayList<>();
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

        ImageView img = findViewById(R.id.imageView);
        img.setImageBitmap(BitmapFactory.decodeFile(filePath));

        TextView title = findViewById(R.id.textView2);
        title.setText(filePath);

        Button back = findViewById(R.id.button6);
        back.setEnabled(false);

        faceDetection();

        try {
            executorService.submit(() -> {
                userIDs = db.faceDao().getAllUsers(filePath);
                for (int userID : userIDs) {
                    names.add((db.userDao().findByUID(userID)).name);
                }
            });
        }
        catch(Exception e){
            Log.e("image view","Error",e);
        }
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
        ImageView img = findViewById(R.id.imageView);
        img.setImageBitmap(BitmapFactory.decodeFile(filePath));

        TextView title = findViewById(R.id.textView2);
        title.setText(filePath);

        Button back = findViewById(R.id.button6);
        back.setEnabled(false);
    }
    public void identify_faces(View v){
        setContentView(R.layout.activity_identify_faces);
        ListView listView= findViewById((R.id.detected_faces_list));
        List<identification_variables> items=new ArrayList<>();
        for(int i=0;i<croppedFaces.size();i++){
            items.add(new identification_variables(croppedFaces.get(i), names.get(i)));
        }
        identify_face_adapter adapter =new identify_face_adapter(this,R.layout.face_identification,items);
        listView.setAdapter(adapter);
    }
    public void onSaveClick(View v){
        ListView listView = findViewById(R.id.detected_faces_list);
        identify_face_adapter adapter = (identify_face_adapter) listView.getAdapter();

        for (int i = 0; i < adapter.getCount(); i++) {
            View item = listView.getChildAt(i);
            if (item != null) {
                EditText editText = item.findViewById(R.id.name);
                String text = editText.getText().toString();
                if(!(text.equals(names.get(i)))){
                    final int index=i;
                    executorService.submit(()-> {
                        User user = db.userDao().findByUID(userIDs.get(index));
                        user.name=text;
                        db.userDao().updateUser(user);
                    });
                }
            }
        }
        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        onResume();
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
            public void onFacesDetected(ArrayList<Rect> faces) {
                rects=faces;
                crop_faces(rects);
                extractEmbeddings();
            }

            @Override
            public void onFaceDetectionFailed(Exception e) {

            }
        };
        File file = new File(filePath);
        Uri uri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", file);
        FaceDetection_Activity faceDetectionActivity= new FaceDetection_Activity();
        faceDetectionActivity.processImage(this,uri);
        faceDetectionActivity.detectFaces(callback);

    }

    protected void crop_faces(ArrayList<Rect>rects) {
        Bitmap image = BitmapFactory.decodeFile(filePath);
        croppedFaces = new ArrayList<>();

        // Crop each face and add to the list
        for (Rect faceRect : rects) {
            Bitmap face = Bitmap.createBitmap(image, faceRect.left, faceRect.top,
                    faceRect.width(), faceRect.height());
            croppedFaces.add(face);
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