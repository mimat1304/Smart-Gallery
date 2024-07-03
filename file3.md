layout file share_screen.xml

<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <ListView
        android:id="@+id/listView"
        android:layout_width="421dp"
        android:layout_height="557dp"
        app:layout_constraintBottom_toTopOf="@+id/constraintLayout3"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

    <androidx.constraintlayout.widget.ConstraintLayout
        android:id="@+id/constraintLayout3"
        android:layout_width="411dp"
        android:layout_height="170dp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@+id/listView">

        <Button
            android:id="@+id/button2"
            android:layout_width="335dp"
            android:layout_height="65dp"
            android:onClick="okShare"
            android:text="OK"
            app:layout_constraintBottom_toTopOf="@+id/button3"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintHorizontal_bias="0.5"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintTop_toTopOf="parent" />

        <Button
            android:id="@+id/button3"
            android:layout_width="335dp"
            android:layout_height="65dp"
            android:onClick="cancelShare"
            android:text="CANCEL"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintHorizontal_bias="0.5"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintTop_toBottomOf="@+id/button2" />
    </androidx.constraintlayout.widget.ConstraintLayout>

</androidx.constraintlayout.widget.ConstraintLayout>





layout file share_item.xml

<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/layout"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical">

    <TextView
        android:id="@+id/name"
        android:layout_width="match_parent"
        android:layout_height="50dp"
        android:layout_marginLeft="20dp"
        android:layout_marginTop="10dp"
        android:text="TextView" />
</LinearLayout>






java file SuggestionsAdapter.java

package com.example.my_application_1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class SuggestionsAdapter extends ArrayAdapter<User> {

    private Context context;
    private int resource;
    private List<User> items;

    public SuggestionsAdapter(@NonNull Context context, int resource, @NonNull List<User> items) {
        super(context, resource, items);
        this.context=context;
        this.resource=resource;
        this.items=items;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(resource, parent, false);
        }

        TextView textView = convertView.findViewById(R.id.name);

        User currentItem = items.get(position);
        String nameStr = currentItem.name;
        if(nameStr=="Unknown"){
            textView.setText("user id "+currentItem.uid);
        }
        textView.setText(nameStr);

        return convertView;
    }
}







main activity java file

package com.example.my_application_1;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements GalleryAdapter.OnImageClickListener, GalleryAdapter.OnImageLongClickListener{

    private GridView galleryListView;
    private GalleryAdapter galleryAdapter;
    private List<String> imagePaths;
    public ArrayList<Rect> rects;
    private ArrayList<Bitmap> croppedFaces;
    private float[][] embeddings;

    List<User>userList=new ArrayList<>();
    private AppDatabase db;
    private ExecutorService userExecutiveService;
    private ExecutorService executiveService;

    //List<Face> Faces;
    //List<Face> toInsert;

    public boolean stateSelection = false;
    public Set<String> selected = new HashSet<String>();
    public Button share;


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

        share = findViewById(R.id.share);
        share.setVisibility(View.INVISIBLE);

        db = AppDatabase.getInstance(getApplicationContext());
        userExecutiveService = Executors.newSingleThreadExecutor();
        executiveService = Executors.newSingleThreadExecutor();

        galleryListView = findViewById(R.id.gallery);
        imagePaths = loadImagesFromGallery();

        galleryAdapter = new GalleryAdapter(this, imagePaths);
        galleryListView.setAdapter(galleryAdapter);
        Log.d("MainActivity", "Starting background thread");
        Thread workThread = new Thread() {
            @Override
            public void run() {
                try {
                    Log.d("MainActivity", "Background thread started");
                    //db.faceDao().clearTable();
                    //db.userDao().clearTable();
                    List<User> userList = db.userDao().getAll();
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


    public void shareGroup(View v){
        //share a bunch of images
        Log.d("share", "button clicked");
        List<String> selectedFiles = new ArrayList<String>();
        selectedFiles.addAll(selected);
        Log.d("share", ""+selectedFiles.size());
        AsyncTask.execute(()->{
            Log.d("share", "started");
            List<Integer> ids = db.faceDao().getAllUsers(selectedFiles);
            HashMap<Integer, Integer> freq = new HashMap<Integer, Integer>();
            for(int i : ids){
                Log.d("share", ""+i);
                if(freq.containsKey(i)){
                    freq.put(i, freq.get(i)+1);
                }else{
                    freq.put(i, 1);
                }
            }
            Collections.sort(ids, new Comparator<Integer>() {
                public int compare(Integer n1, Integer n2){
                    int freq1 = freq.get(n1);
                    int freq2 = freq.get(n2);
                    if (freq1 != freq2) {
                        return freq2 - freq1;
                    }else {
                        return (n1 - n2);
                    }
                }
            });
            List<User> suggestions = new ArrayList<User>();
            for(int i =0 ; i< ids.size() ; i++){
                if(i>0 && ids.get(i)==ids.get(i-1)) continue;
                User temp = db.userDao().findByUID(ids.get(i));
                if(!suggestions.contains(temp)){
                    suggestions.add(temp);
                }
            }
            Log.d("share", "suggestions");
            for(User user : suggestions){
                Log.d("share", ""+user.uid);
            }
            Log.d("share", "printed "+suggestions.size());

            try {
                runOnUiThread(() -> {
                    setContentView(R.layout.share_screen);
                    ListView shareListView = findViewById(R.id.listView);
                            SuggestionsAdapter suggestionsAdapter = new SuggestionsAdapter(this, R.layout.share_item, suggestions);
                            shareListView.setAdapter(suggestionsAdapter);
                        }
                );
            }catch (Exception e){
                e.printStackTrace();
                Log.d("error", "error");
            }

        });

    }

    public void okShare(View v){
        selected.clear();
        setContentView(R.layout.activity_main);
        galleryListView = findViewById(R.id.gallery);
        galleryAdapter = new GalleryAdapter(this, imagePaths);
        galleryListView.setAdapter(galleryAdapter);
        share = findViewById(R.id.share);
        share.setVisibility(View.INVISIBLE);
        stateSelection = false;
    }
    public void cancelShare(View v){
        selected.clear();
        setContentView(R.layout.activity_main);
        galleryListView = findViewById(R.id.gallery);
        galleryAdapter = new GalleryAdapter(this, imagePaths);
        galleryListView.setAdapter(galleryAdapter);
        share = findViewById(R.id.share);
        share.setVisibility(View.INVISIBLE);
        stateSelection = false;
    }
    @Override
    public void onImageClick(String imagePath, ImageView imV) {
        if(stateSelection){
            if(selected.contains(imagePath)){
                selected.remove(imagePath);
                imV.setBackgroundColor(Color.TRANSPARENT);
                imV.setColorFilter(null);
                if(selected.isEmpty()){
                    stateSelection = false;
                    share.setVisibility(View.INVISIBLE);
                }
            }else{
                selected.add(imagePath);
                imV.setBackgroundColor(Color.parseColor("#AA0000FF"));
                imV.setColorFilter(Color.argb(150,0,0,0));
            }
            return;
        }
        Toast.makeText(this, "Clicked on image: " + imagePath, Toast.LENGTH_SHORT).show();

        Intent openPhoto = new Intent(this, image_view.class);
        openPhoto.putExtra("filePath", imagePath);
        startActivity(openPhoto);
    }
    @Override
    public boolean onImageLongClick(String imagePath, ImageView imV) {
        Log.d("samsung", "long clicked "+imagePath);
        stateSelection = true;
        share.setVisibility(View.VISIBLE);
        if(selected.contains(imagePath)){
            selected.remove(imagePath);
            imV.setBackgroundColor(Color.TRANSPARENT);
            imV.setColorFilter(null);
            if(selected.isEmpty()){
                stateSelection = false;
                share.setVisibility(View.INVISIBLE);
            }
        }else{
            selected.add(imagePath);
            imV.setBackgroundColor(Color.parseColor("#AA0000FF"));
            imV.setColorFilter(Color.argb(150,0,0,0));
        }
        return true;
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
        return imagePaths;
    }

    private void processData(){
        //long sTime = System.nanoTime();
        Set<String> fileSet = new HashSet<String>();
        int count =0;
        List<String> fileList = db.faceDao().loadAllFiles();
        fileSet.addAll(fileList);
        for(String filePath: imagePaths){
            //Faces=db.faceDao().loadByFilePath(filePath);
            if(fileSet.contains(filePath)) {
                //faceDetection(filePath);
                //Log.d("file found", filePath);
                count++;
            }
            else{
                faceDetection(filePath);
            }
        }
        //long eTime = System.nanoTime();
        Log.d("file found", ""+count);
        //Log.d("file found", ""+(eTime-sTime));
    }
    protected void faceDetection(String filePath){
        FaceDetectionCallback callback= new FaceDetectionCallback() {
            @Override
            public void onFacesDetected(ArrayList<Rect> faces) {
                userExecutiveService.submit(() -> {
                    rects = faces;
                    crop_faces(filePath);
                    extractEmbeddings(filePath);
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
    protected void extractEmbeddings(String filePath){
        try {
            FaceNetEmbeddings faceNetEmbeddings = new FaceNetEmbeddings(this,"facenet.tflite");
            embeddings = faceNetEmbeddings.getEmbeddings(croppedFaces);
            Log.d("Embeddings status:", "Generated");
            updateData(filePath);
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    protected void updateData(String filePath){
        for(float[] em:embeddings){
            checkUsers(em,filePath);
        }
    }
    private void checkUsers(float[] cur_em,String filePath){
        boolean flag = false;
        float threshold=0.63f;
        for(int i=0;i<userList.size();i++){
            User user= userList.get(i);
            float[] em=user.embeddings;
            if (similarity(em,cur_em)>=threshold){
                user.embeddings=meanEmbeddings(em, cur_em, user.n);
                user.n=user.n+1;
                Face face =new Face(cur_em,filePath,i+1);
                userList.set(i,user);
                db.userDao().updateUser(user);
                Log.d("Main Activity","User updated");
                db.faceDao().insert(face);
                Log.d("Main Activity","Face Inserted");
                flag=true;
                break;
            }
        }
        if(!flag){
            // prompt user to enter name
            User user= new User("Unknown", 1, cur_em);
            userList.add(user);
            Face face =new Face(cur_em,filePath,userList.size());
            db.userDao().insert(user);
            Log.d("Main Activity","User Inserted");
            db.faceDao().insert(face);
            Log.d("Main Activity","Face Inserted");
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
