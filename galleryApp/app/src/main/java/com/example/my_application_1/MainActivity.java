package com.example.my_application_1;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements GalleryAdapter.OnImageClickListener {

    private GridView galleryListView;
    private GalleryAdapter galleryAdapter;
    private List<String> imagePaths;
    public ArrayList<Rect>rects;
    private ArrayList<Bitmap> croppedFaces;
    private float[][] embeddings;

    List<User>userList=new ArrayList<>();
    private AppDatabase db;
    private ExecutorService userExecutiveService;
    int allFilesSize;
    List<Face> facesToInsert= new ArrayList<>();
    List<User> usersToUpdate= new ArrayList<>();
    List<User> usersToInsert= new ArrayList<>();
    int fkey=0;

    private static final int REQUEST_CODE_PERMISSIONS = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_CODE_PERMISSIONS);
        }

        db = AppDatabase.getInstance(getApplicationContext());
        userExecutiveService = Executors.newSingleThreadExecutor();

        galleryListView = findViewById(R.id.gallery);
        imagePaths = loadImagesFromGallery();

        galleryAdapter = new GalleryAdapter(this, imagePaths, this);
        galleryListView.setAdapter(galleryAdapter);
        Log.d("MainActivity", "Starting background thread");
        Thread workThread = new Thread() {
            @Override
            public void run() {
                try {
                    Log.d("MainActivity", "Background thread started");
                    List<User> userList = db.userDao().getAll();
                    if(userList.size()>0){
                        fkey=userList.get(userList.size()-1).fkey+1;
                    }
                    Log.d("MainActivity", "UserList size= " + userList.size());

                    processData();
                } catch (Exception e) {
                    Log.e("MainActivity", "Error fetching user list", e);
                }
                Log.d("MainActivity", "Background thread finished");
            }
        };
        workThread.start();
        Log.d("MainActivity", "Background thread initiated");
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                ((Button)findViewById(R.id.shareButton)).setEnabled(false);
            }
        }
    }

    @Override
    public void onImageClick(String imagePath) {
        Toast.makeText(this, "Clicked on image: " + imagePath, Toast.LENGTH_SHORT).show();

        Intent openPhoto = new Intent(this, image_view.class);
        openPhoto.putExtra("filePath", imagePath);
        startActivity(openPhoto);
    }

    private List<String> loadImagesFromGallery() {
        List<String> imagePaths = new ArrayList<>();
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = { MediaStore.MediaColumns.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
                imagePaths.add(path);
            }
            cursor.close();
        }
        allFilesSize=imagePaths.size();
        return imagePaths;
    }

    private void processData(){
        Set<String> fileSet = new HashSet<>();
        List<String> allFiles= db.faceDao().loadAllFiles();
        fileSet.addAll(allFiles);
        int count=0;
        int ptr=0;
        for(String filePath: imagePaths){
            ptr++;
            if(fileSet.contains(filePath)) {
                count++;
//                Log.d("Image count",""+count);
                if(ptr==allFilesSize){
                    db.userDao().updateAllUser(usersToUpdate);
                    db.userDao().insertAll(usersToInsert);
                    db.faceDao().insertAll(facesToInsert);
                    Log.d("processData","Database updated");
                }
            }
            else{
                faceDetection(filePath,ptr);
            }
        }
        Log.d("Total new files",""+(allFilesSize-count));
    }
    protected void faceDetection(String filePath,int ptr){
        FaceDetectionCallback callback= new FaceDetectionCallback() {
            @Override
            public void onFacesDetected(ArrayList<Rect> faces) {
                userExecutiveService.submit(() -> {
//                    Log.d("Task","Task started");
                    rects = faces;
                    crop_faces(filePath);
                    extractEmbeddings(filePath,ptr);
//                    Log.d("Task","Task completed");
                });
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
    protected void crop_faces(String filePath) {
        Bitmap image = BitmapFactory.decodeFile(filePath);
        croppedFaces = new ArrayList<>();

        // Crop each face and add to the list
        for (Rect faceRect : rects) {
            try{
            Bitmap face = Bitmap.createBitmap(image, faceRect.left, faceRect.top,
                    faceRect.width(), faceRect.height());
            croppedFaces.add(face);}
            catch(Exception e){

            }
        }
    }
    protected void extractEmbeddings(String filePath,int ptr){
        try {
            FaceNetEmbeddings faceNetEmbeddings = new FaceNetEmbeddings(this,"facenet.tflite");
            embeddings = faceNetEmbeddings.getEmbeddings(croppedFaces);
//            Log.d("Embeddings status:", "Generated");
            updateData(filePath,ptr);
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    protected void updateData(String filePath,int ptr){
        for(float[] em:embeddings){
            checkUsers(em,filePath,ptr);
        }
    }
    private void checkUsers(float[] cur_em,String filePath,int ptr){
        boolean flag = false;
        float threshold=0.63f;
        for(int i=0;i<userList.size();i++){
            User user= userList.get(i);
            float[] em=user.embeddings;
            if (similarity(em,cur_em)>=threshold){
                user.embeddings=meanEmbeddings(em, cur_em, user.n);
                user.n=user.n+1;
                Face face =new Face(cur_em,filePath,user.fkey);
                userList.set(i,user);
                usersToUpdate.add(user);
//                db.userDao().updateUser(user);
//                Log.d("Main Activity","User updated");
                facesToInsert.add(face);
//                db.faceDao().insert(face);
//                Log.d("Main Activity","Face Inserted");
                flag=true;
                break;
            }
        }
        if(!flag){
            // prompt user to enter name
            User user= new User("Unknown", 1, cur_em,fkey++);
            Face face =new Face(cur_em,filePath,user.fkey);
            userList.add(user);
//            db.userDao().insert(user);
            usersToInsert.add(user);
//            Log.d("Main Activity","User Inserted");
            facesToInsert.add(face);
//            db.faceDao().insert(face);
//            Log.d("Main Activity","Face Inserted");
        }
        if(ptr==allFilesSize){
            try{
            db.userDao().updateAllUser(usersToUpdate);
            db.userDao().insertAll(usersToInsert);
            db.faceDao().insertAll(facesToInsert);
            usersToUpdate.clear();
            usersToInsert.clear();
            facesToInsert.clear();
            Log.d("checkUser","Database updated");}
            catch (Exception e){
                Log.e("usersCheck","Error",e);
            }
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
    @Override
    protected void onDestroy() {
        super.onDestroy();
        userExecutiveService.shutdown();
        //        The ExecutorService shutdown in the onDestroy method ensures that:
        //        No new tasks will be accepted after the activity is destroyed.
        //        Currently running tasks are allowed to complete.
    }

}
