package com.example.my_application_1;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;
@Dao
public interface UserDao {
    @Query("SELECT * FROM User")
    List<User> getAll();

    @Query("SELECT * FROM User WHERE uid IN (:userIds)")
    List<User> loadAllByIds(int[] userIds);

    @Query("SELECT * FROM User WHERE uid = :uid")
    User findByUID(int uid);

    @Insert
    void insert(User user);

    @Insert
    void insertAll(List<User>users);

    @Delete
    void deleteAll(List<User>users);

    @Delete
    void delete(User user);

    @Query("DELETE FROM User")
    void clearTable();

}
