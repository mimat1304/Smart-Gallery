package com.example.my_application_1;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.content.Context;
import android.util.Log;
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
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FaceDetection_Activity{
    InputImage image;
    float imgWidth,imgHeight;
    protected void processImage(Context context,Bitmap bitmap){
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        imgHeight=bitmap.getHeight();
        imgWidth=bitmap.getWidth();
        image = InputImage.fromBitmap(bitmap,0);
        Log.d("ImageWidth",imgWidth+" "+image.getWidth());
    }
    protected void detectFaces(FaceDetectionCallback callback){
        FaceDetector detector = FaceDetection.getClient();
        Task<List<Face>> result =
                detector.process(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<Face>>() {
                                    @Override
                                    public void onSuccess(List<Face> faces) {
                                        // Task completed successfully
                                        // ...
                                        List<Float> rollAngles= new ArrayList<>();
                                        float mxWidth = 0.01f;
                                        Log.d("faces size",""+faces.size());
                                        for( Face face:faces){
                                            Rect bounds = face.getBoundingBox();
                                            Log.d("widths",""+bounds.width());
                                            mxWidth = Math.max(mxWidth,bounds.width());
                                        }
                                        detectFacesWithThreshold(callback,mxWidth);
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
    protected void detectFacesWithThreshold(FaceDetectionCallback callback, float mxWidth){
        float minFaceSize = 0.45f * mxWidth ;
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
                                        List<Float> rollAngles= new ArrayList<>();
                                        Log.d("faces size",""+faces.size());
                                        for( Face face:faces){
                                            Rect bounds = face.getBoundingBox();
                                            if(bounds.width() < minFaceSize){
                                                continue;
                                            }
                                            rollAngles.add(face.getHeadEulerAngleZ());
                                            rects.add(bounds);
                                        }
                                        Log.d("rectsSize",""+rects.size());
                                        callback.onFacesDetected(rects,rollAngles);
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
