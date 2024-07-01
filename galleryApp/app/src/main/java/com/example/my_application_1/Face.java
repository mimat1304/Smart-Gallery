package com.example.my_application_1;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

@Entity(indices = {@Index(value = {"filePath"})})
public class Face {
    @PrimaryKey(autoGenerate = true)
    public int uid;
    @TypeConverters(Converters.class)
    public float[] embeddings;
    @ColumnInfo(name="filePath")
    String filePath;

    @ColumnInfo(name="fKey")
    public int fKey;

    public Face(float[] embeddings,String filePath,int fKey){
        this.embeddings=embeddings;
        this.filePath=filePath;
        this.fKey=fKey;
    }
}
