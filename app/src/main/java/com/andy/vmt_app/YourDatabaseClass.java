package com.andy.vmt_app;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {WordGroup.class}, version = 1)
@TypeConverters({WordTypeConverter.class})
public abstract class YourDatabaseClass extends RoomDatabase {

    private static YourDatabaseClass instance;

    public abstract WordGroupDao wordGroupDao();

    public static synchronized YourDatabaseClass getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            YourDatabaseClass.class, "your_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}

