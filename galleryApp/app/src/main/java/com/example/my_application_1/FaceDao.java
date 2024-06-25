package com.example.my_application_1;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface FaceDao {
    @Insert
    public void insertAll(List<Face> face);

    @Query("SELECT * FROM Face WHERE uid = :uid")
    Face findByUID(int uid);

    @Query("SELECT * FROM Face WHERE uid IN (:userIds)")
    List<Face> loadAllByIds(int[] userIds);

    @Delete
    void delete(Face face);

}
