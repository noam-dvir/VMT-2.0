package com.andy.vmt_app;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.io.Serializable;

public class Word implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String wordToLearn;
    public String translation;

    public Word(String wordToLearn, String translation) {
        this.wordToLearn = wordToLearn;
        this.translation = translation;
    }

}
