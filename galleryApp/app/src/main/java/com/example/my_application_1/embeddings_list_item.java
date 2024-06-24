package com.example.my_application_1;

import android.graphics.Bitmap;

public class embeddings_list_item {
    private Bitmap imageBitmap;
    private String embeddings;
    public embeddings_list_item(Bitmap imageBitmap, String embeddings){
        this.imageBitmap=imageBitmap;
        this.embeddings=embeddings;
    }

    public Bitmap getBitmap() {
        return imageBitmap;
    }

    public String getEmbeddings() {
        return embeddings;
    }
}
