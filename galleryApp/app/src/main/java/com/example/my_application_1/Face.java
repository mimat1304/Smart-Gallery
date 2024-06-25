package com.example.my_application_1;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

@Entity
public class Face {
    @PrimaryKey(autoGenerate = true)
    public int uid;
    @TypeConverters(Converters.class)
    public float[] embeddings;

    public Face(float[] embeddings){
        this.embeddings=embeddings;
    }
}
