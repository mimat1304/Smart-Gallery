package com.example.my_application_1;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
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


public class image_view extends AppCompatActivity {


    public String filePath;
    public ArrayList<Rect>rects;
    private ArrayList<Bitmap> croppedFaces;
    private float[][] embeddings;
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
            // use of embeddings to be implemented
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
}