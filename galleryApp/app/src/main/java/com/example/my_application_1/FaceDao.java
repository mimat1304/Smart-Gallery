package com.example.my_application_1;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface FaceDao {
    @Insert
    void insertAll(List<Face> face);

    @Insert
    void insert(Face face);
    @Query("SELECT * FROM Face WHERE fKey = :fKey")
    Face findByfKey(int fKey);

    @Query("SELECT filePath from Face")
    List<String> loadAllFiles();
    @Query("SELECT * FROM Face WHERE filePath =:filePath")
    List<Face> loadByFilePath(String filePath);
    @Query("SELECT * FROM Face WHERE uid IN (:userIds)")
    List<Face> loadAllByIds(int[] userIds);

    @Delete
    void delete(Face face);
    @Query("DELETE FROM Face")
    void clearTable();
}
