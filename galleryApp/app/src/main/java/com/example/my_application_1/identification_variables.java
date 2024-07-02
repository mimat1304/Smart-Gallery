package com.example.my_application_1;

import android.graphics.Bitmap;

public class identification_variables {
    private Bitmap imageBitmap;
    private String name;
    public identification_variables(Bitmap imageBitmap, String name){
        this.imageBitmap=imageBitmap;
        this.name=name;
    }

    public Bitmap getBitmap() {
        return imageBitmap;
    }

    public String getName() {
        return name;
    }
}
