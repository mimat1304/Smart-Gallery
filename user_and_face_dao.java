faceDao

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
    @Query("SELECT * FROM Face WHERE filePath =:filePath")
    List<Face> loadByFilePath(String filePath);
    @Query("SELECT * FROM Face WHERE userID IN (:userIds)")
    List<Face> loadAllByIds(int[] userIds);

    @Query("SELECT * FROM Face WHERE uid = :uid")
    Face getFaceFromUid(int uid);

    @Query("SELECT uid FROM face WHERE filePath = :filePath")
    List<Integer> getAllFaceIds(String filePath);

    @Delete
    void delete(Face face);
    @Query("DELETE FROM Face")
    void clearTable();

    @Update
    void updateFace(Face face);
}












userdao

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
    @Query("SELECT * FROM User WHERE uid IN (:userIds)")
    List<User> loadAllByIds(List<Integer> userIds);

    @Query("SELECT * FROM User WHERE uid = :uid")
    User findByUID(int uid);

    @Query("SELECT * FROM User WHERE name = :name LIMIT 1")
    User findByName(String name);

    @Query("SELECT COUNT(*) FROM User")
    int getUserListSize();

    @Insert
    void insert(User user);

    @Insert
    void insertAll(List<User>users);

    @Delete
    void deleteAll(List<User>users);

    @Delete
    void delete(User user);
    @Update
    void updateUser(User user);

    @Update
    void updateAllUser(List<User>users);

    @Query("DELETE FROM User")
    void clearTable();

}
