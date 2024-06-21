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
import java.util.ArrayList;




public class image_view extends AppCompatActivity {


    public String filePath;
    public ArrayList<Rect>rects;
    private ArrayList<Bitmap> croppedFaces;
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
                Log.d("No._of_faces_detected: ",""+rects.size());
                int ptr=0;
                for(Rect rect:rects){
                    ptr++;
                    Log.d("Coordinates of face "+ptr," left: "+rect.left+" right: "+rect.right+" top: "+rect.top+" bottom: "+rect.bottom);
                }
                crop_faces(rects);
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

        // Set up the adapter and ListView
        ListView listView = findViewById(R.id.face_list_view);
        FacesAdapter adapter = new FacesAdapter(this, croppedFaces);
        listView.setAdapter(adapter);
    }
}