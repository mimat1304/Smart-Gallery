package com.example.my_application_1;

import android.graphics.Rect;

import java.util.ArrayList;
import java.util.List;

public interface FaceDetectionCallback {
    void onFacesDetected(ArrayList<Rect> faces, List<Float> rollAngles);
    void onFaceDetectionFailed(Exception e);
}
