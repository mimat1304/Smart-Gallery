package com.example.my_application_1;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;

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
    }
    protected void detectFaces(FaceDetectionCallback callback){
        float thresholdRatio=0.45f;
        FaceDetector detector = FaceDetection.getClient();
        Task<List<Face>> result =
                detector.process(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<Face>>() {
                                    @Override
                                    public void onSuccess(List<Face> faces) {
                                        // Task completed successfully
                                        // ...
                                        List<Rect>rects=new ArrayList<>();
                                        ArrayList<Rect>finalRects=new ArrayList<>();
                                        List<Float> rollAngles= new ArrayList<>();
                                        List<Float> finalRollAngles= new ArrayList<>();

                                        float mxWidth = 0.01f;
                                        for( Face face:faces){
                                            Rect bounds = face.getBoundingBox();
                                            mxWidth = Math.max(mxWidth,bounds.width());
                                            rects.add(bounds);
                                            rollAngles.add(face.getHeadEulerAngleZ());
                                        }
                                        for(int i=0;i<rects.size();i++){
                                            Rect rect= rects.get(i);
                                            float rollAngle=rollAngles.get(i);
                                            if(rect.width() >= thresholdRatio * mxWidth) {
                                                finalRects.add(rect);
                                                finalRollAngles.add(rollAngle);
                                            }
                                        }
                                        callback.onFacesDetected(finalRects,finalRollAngles);
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
