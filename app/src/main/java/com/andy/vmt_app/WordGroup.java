package com.andy.vmt_app;

// WordGroup.java
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.List;

@Entity(tableName = "word_group_table")
public class WordGroup {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;

    @TypeConverters(WordTypeConverter.class)
    public List<Word> words;

    public WordGroup(String groupName, List<Word> wordsList) {
        name = groupName;
        words = wordsList;
    }

    public WordGroup() {

    }

    // Constructors, getters and setters
}
