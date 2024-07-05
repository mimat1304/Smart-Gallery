package com.example.my_application_1;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface FaceDao {
    @Insert
    void insertAll(List<Face> face);

    @Insert
    void insert(Face face);

    @Query("SELECT userId FROM face WHERE filePath IN (:filePaths)")
    List<Integer> getAllUsers(List<String> filePaths);
    @Query("SELECT userId FROM face WHERE filePath =:filePath")
    List<Integer> getAllUsers(String filePath);
    @Query("SELECT filePath from Face")
    List<String> loadAllFiles();
    @Query("SELECT * FROM Face WHERE uid = :uid")
    Face getFaceFromUid(int uid);
    @Update
    void updateFace(Face face);
    @Query("SELECT * FROM Face WHERE filePath =:filePath")
    List<Face> loadByFilePath(String filePath);
    @Query("SELECT * FROM Face WHERE uid IN (:userIds)")
    List<Face> loadAllByIds(int[] userIds);

    @Delete
    void delete(Face face);
    @Query("DELETE FROM Face")
    void clearTable();
}
