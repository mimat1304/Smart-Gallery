package com.example.my_application_1;

import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FaceDetection_Activity extends AppCompatActivity{
    InputImage image;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    protected void processImage(Context context,Uri uri){
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }

        if (uri == null) {
            throw new IllegalArgumentException("Uri cannot be null");
        }
        try {
            image = InputImage.fromFilePath(context, uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    protected void detectFaces(FaceDetectionCallback callback){
        FaceDetector detector = FaceDetection.getClient();
        ArrayList<Rect> rects=new ArrayList<>();
        Task<List<Face>> result =
                detector.process(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<Face>>() {
                                    @Override
                                    public void onSuccess(List<Face> faces) {
                                        // Task completed successfully
                                        // ...
                                        for( Face face:faces){
                                            Rect bounds = face.getBoundingBox();
                                            rects.add(bounds);
                                        }
                                        callback.onFacesDetected(rects);
                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                        callback.onFaceDetectionFailed(e);
                                    }
                                });
    }

}
