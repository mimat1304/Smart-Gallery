package com.example.my_application_1;

import android.graphics.Rect;

import java.util.ArrayList;

public interface FaceDetectionCallback {
    void onFacesDetected(ArrayList<Rect> faces);
    void onFaceDetectionFailed(Exception e);
}
