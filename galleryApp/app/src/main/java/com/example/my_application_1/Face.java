package com.example.my_application_1;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

@Entity(indices = {@Index(value = {"filePath"})},
        foreignKeys = {@ForeignKey(entity = User.class,parentColumns = "uid",childColumns = "userID",onDelete = ForeignKey.CASCADE)})
public class Face {
    @PrimaryKey(autoGenerate = true)
    public int uid;
    @TypeConverters(Converters.class)
    public float[] embeddings;
    @ColumnInfo(name="filePath")
    String filePath;
    @ColumnInfo(name="userID")
    public int userID;

    public Face(float[] embeddings,String filePath,int userID){
        this.embeddings=embeddings;
        this.filePath=filePath;
        this.userID=userID;
    }
}
