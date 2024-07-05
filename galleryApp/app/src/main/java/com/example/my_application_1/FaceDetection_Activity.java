package com.example.my_application_1;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.media.ExifInterface;
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

public class FaceDetection_Activity{
    InputImage image;
    protected void processImage(Context context,String filePath){
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }

        try {
            Bitmap bitmap = BitmapFactory.decodeFile(filePath);
            int rotationAngle=getRotationAngle(filePath);
            image = InputImage.fromBitmap(bitmap, rotationAngle);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    protected int getRotationAngle(String imagePath) throws IOException {
        ExifInterface ei = new ExifInterface(imagePath);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return 90;
            case ExifInterface.ORIENTATION_ROTATE_180:
                return 180;
            case ExifInterface.ORIENTATION_ROTATE_270:
                return 270;
            default:
                return 0;
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
                                        List<Float> rollAngles= new ArrayList<>();
                                        for( Face face:faces){
                                            Rect bounds = face.getBoundingBox();
                                            rollAngles.add(face.getHeadEulerAngleZ());
                                            rects.add(bounds);
                                        }
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
