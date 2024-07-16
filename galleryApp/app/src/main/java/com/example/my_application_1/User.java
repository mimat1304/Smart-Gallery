package com.example.my_application_1;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

@Entity(tableName = "User")
public class User {
    @PrimaryKey
    public int uid;
    @ColumnInfo(name = "Number of images")
    public int n;

    @ColumnInfo(name = "Name")
    public String name;

    @ColumnInfo(name = "embeddings")
    @TypeConverters(Converters.class)
    public float[] embeddings;

    public User(String name,int n, float[] embeddings) {
        this.n = n;
        this.name = name;
        this.embeddings = embeddings;
    }
}
