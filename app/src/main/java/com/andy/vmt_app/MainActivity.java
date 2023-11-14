package com.andy.vmt_app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private WordGroupAdapter adapter;
    private List<WordGroup> wordGroups = new ArrayList<>();
    public WordGroupDao wordGroupDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        YourDatabaseClass db = Room.databaseBuilder(getApplicationContext(),
                YourDatabaseClass.class, "your_database").build();
        wordGroupDao = db.wordGroupDao();

        wordGroupDao.getAllWordGroups().observe(this, new Observer<List<WordGroup>>() {
            @Override
            public void onChanged(@Nullable List<WordGroup> wordGroupsFromDb) {
                wordGroups.clear();
                if (wordGroupsFromDb != null) {
                    wordGroups.addAll(wordGroupsFromDb);
                }
                adapter.notifyDataSetChanged();
            }
        });


        recyclerView = findViewById(R.id.recycler_view);
        //adapter = new WordGroupAdapter(wordGroups, this);
        adapter = new WordGroupAdapter(wordGroups, this, new WordGroupAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(WordGroup wordGroup) {

                //check if group is empty
                if (wordGroup.words == null || wordGroup.words.size()==0) {
                    showEmptyGroupDialog();
                } else {
                    //init activity
                    Intent intent = new Intent(MainActivity.this, WordActivity.class);
                    intent.putExtra("wordList", (Serializable) wordGroup.words);
                    startActivity(intent);
                }
            }
        });

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        FloatingActionButton fabAdd = findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle the click event here; for instance, open a new activity
                Intent intent = new Intent(MainActivity.this, GroupActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        new Thread(() -> {
            List<WordGroup> wordGroupsFromDb = wordGroupDao.getAllWordGroupsStatic();

            runOnUiThread(() -> {
                wordGroups.clear();
                wordGroups.addAll(wordGroupsFromDb);
                adapter.notifyDataSetChanged();
            });
        }).start();
    }

    private void showEmptyGroupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Empty group. Add words.")
                .setPositiveButton("OK", (dialog, id) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}

