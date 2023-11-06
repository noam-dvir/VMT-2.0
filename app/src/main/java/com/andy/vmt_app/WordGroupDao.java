package com.andy.vmt_app;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface WordGroupDao {
    @Query("SELECT * FROM word_group_table")
    LiveData<List<WordGroup>> getAllWordGroups();

    @Query("SELECT * FROM word_group_table")
    List<WordGroup> getAllWordGroupsStatic();

    @Query("SELECT * FROM word_group_table WHERE id = :groupId")
    WordGroup getWordGroupById(int groupId);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(WordGroup wordGroup);

    @Update
    void update(WordGroup wordGroup);

    @Delete
    void delete(WordGroup wordGroup);
}

