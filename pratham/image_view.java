package com.example.my_application_1;

import android.content.Intent;
import android.content.pm.PackageManager;
//import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
//import android.provider.MediaStore;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.Manifest;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class image_view extends AppCompatActivity {


    private static final int REQUEST_CODE_PERMISSIONS = 100;
    private static String IMAGE_PATH = "Downloads/download.jpg";

    public final List<String> filesList = new ArrayList<>();

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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_CODE_PERMISSIONS);
        }else{
            //setupShareButton();
        }
        //loadImages();
        //ImageView img = (ImageView)findViewById((R.id.imageView));

        final File imageDir= new File(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)));
        final File[] files= imageDir.listFiles();
        final int filesCount;
        if(files!=null)// files may be a null pointer
            filesCount=files.length;
        else filesCount=0;

        for(int i=0;i<filesCount;i++){
            final String path =files[i].getAbsolutePath();
            if(path.endsWith(".jpg") || path.endsWith(".png") || path.endsWith(".jpeg")){
                filesList.add(path);
            }
        }
        ImageView img = findViewById(R.id.imageView);
        img.setImageBitmap(BitmapFactory.decodeFile(filesList.get(0)));

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //setupShareButton();
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
                Log.d("permissionDenied", "Permission Granted");
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                Log.d("permissionDenied", "Permission Denied");
                ((Button)findViewById(R.id.shareButton)).setEnabled(false);
            }
        }
    }
    public void shareImage(View v) {
        ((Button)v).setEnabled(false);
        //img.setImageResource(R.drawable.sig);
        File file = new File(filesList.get(0));
        Log.d("logfile", "file created");
        //Uri uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", file);
        Uri uri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", file);
        Log.d("logfile", "uri found");
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.setType("image/jpeg");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Log.d("logfile", "intent created");
        startActivity(Intent.createChooser(shareIntent, "Share Image"));
        Log.d("logfile", "intent activated");
    }

    /*private void loadImages() {
        final String[] colums={MediaStore.Images.Media.DATA,MediaStore.Images.Media._ID};
        final String order=MediaStore.Images.Media.DATE_TAKEN+" DESC";
        Cursor cursor=getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,colums,null,null,order);
            /*int count =cursor.getCount();
            for(int i=0;i<count;i++){
                cursor.moveToPosition(i);
                int colunmindex=cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                images.add(cursor.getString(colunmindex));
            *//*
        cursor.moveToPosition(0);
        int colunmindex=cursor.getColumnIndex(MediaStore.Images.Media.DATA);
        IMAGE_PATH = cursor.getString(colunmindex);

        }*/
}