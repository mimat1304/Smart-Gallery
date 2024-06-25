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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

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
    private AppDatabase db;
    private ExecutorService faceExecutorService;
    private ExecutorService userExecutiveService;
    private List<User> userList;
    private final float threshold = 0.7f;
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

        db = AppDatabase.getInstance(getApplicationContext());
        faceExecutorService = Executors.newSingleThreadExecutor();
        userExecutiveService = Executors.newSingleThreadExecutor();

        Intent intent = getIntent();
        filePath = intent.getStringExtra("filePath");

        ImageView img = findViewById(R.id.imageView);
        img.setImageBitmap(BitmapFactory.decodeFile(filePath));

        TextView title = findViewById(R.id.textView2);
        title.setText(filePath);

        Button back = findViewById(R.id.button6);
        back.setEnabled(false);

        Button delete = findViewById(R.id.deleteButton);
        delete.setEnabled(false);
        faceDetection();
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

            updateData();
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
    protected void updateData(){
        // if(uid not present in image)
            List<Face> faces= new ArrayList<>();
            for(float[] em:embeddings){
                faces.add(new Face(em));
            }
            insertFace(faces);

        UserCallback callback= new UserCallback() {
            @Override
            public void onUsersListComplete() {
                Log.d("Users List","Fetched");
                for(float[] em:embeddings) {
                    checkUsers(em);
                }
            }

            @Override
            public void insertComplete() {

            }
        };

        getAllUsers(callback);


    }
    private void insertFace(List<Face> faces){
        faceExecutorService.execute(() -> db.faceDao().insertAll(faces));
    }
    private void getAllUsers(UserCallback callback){
        userExecutiveService.execute(()->{userList=db.userDao().getAll();callback.onUsersListComplete();});
    }
    private void checkUsers(float[] cur_em){
        boolean flag = false;
        for(User user: userList){
            float[] em=user.embeddings;
            if (similarity(em,cur_em)>=threshold){
                userExecutiveService.execute(()->db.userDao().delete(user));
                if(user.name.equals("Unknown")){
                    //prompt user to enter name
                }
                user.embeddings=meanEmbeddings(em, cur_em, user.n);
                user.n=user.n+1;
                userExecutiveService.execute(()-> db.userDao().insertAll(user));
                flag=true;
            }
        }
        if(!flag){
            // prompt user to enter name
            userExecutiveService.execute(()->db.userDao().insertAll(new User("Unknown",1,cur_em)));
        }
    }
    private float[] meanEmbeddings(float[] cur_em,float[] em,int n){
        float[] meanEM= new float[em.length];
        for (int i=0;i<em.length;i++){
            meanEM[i]=(em[i]*n+cur_em[i])/(n+1);
        }
        return meanEM;
    }
    private float similarity(float[] em1, float[] em2){
        float dotProduct = 0.0f;
        for (int i = 0; i < em1.length; i++) {
            dotProduct += em1[i] * em2[i];
        }
        float magnitude1=0.0f,magnitude2=0.0f;
        for (int i=0;i< em1.length;i++){
            magnitude1+=(em1[i]*em1[i]);
            magnitude2+=(em2[i]*em2[i]);
        }
        magnitude1 = (float) Math.sqrt(magnitude1);
        magnitude2 = (float) Math.sqrt(magnitude2);
        return dotProduct/(magnitude1*magnitude2);
    }
    private interface UserCallback{
        void onUsersListComplete();
        void insertComplete();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        faceExecutorService.shutdown();
        userExecutiveService.shutdown();
//        The ExecutorService shutdown in the onDestroy method ensures that:
//        No new tasks will be accepted after the activity is destroyed.
//        Currently running tasks are allowed to complete.
    }
}