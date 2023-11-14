package com.andy.vmt_app;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GroupActivity extends AppCompatActivity implements WordAdapter.OnWordClickListener {
    private EditText groupNameEditText;
    private RecyclerView wordsRecyclerView;
    private WordAdapter wordAdapter;
    private Button saveButton;
    private Button addWordButton;
    private ImageButton switchAllButton;

    //private int wordGroupId = -1;

    private YourDatabaseClass database;
    public WordGroupDao wordGroupDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);  // Set the layout file for this activity

        //Init DAO
        database = Room.databaseBuilder(getApplicationContext(),
                YourDatabaseClass.class, "your_database").build();
        wordGroupDao = database.wordGroupDao();

        //UI elements
        groupNameEditText = findViewById(R.id.group_name_edit_text);
        wordsRecyclerView = findViewById(R.id.words_recycler_view);
        saveButton = findViewById(R.id.save_button);
        addWordButton = findViewById(R.id.add_word_button);
        Button addBulkButton = findViewById(R.id.add_bulk_button);
        Toolbar toolbar = findViewById(R.id.toolbar);
        switchAllButton = findViewById(R.id.switch_all_button);

        wordAdapter = new WordAdapter(new ArrayList<>(), this);
        wordsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        wordsRecyclerView.setAdapter(wordAdapter);

        //Init toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Display the back arrow
        getSupportActionBar().setDisplayShowHomeEnabled(true); // Make it clickable

        toolbar.setNavigationIcon(R.drawable.ic_back); // Set your icon here
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackButtonPressed();
            }
        });

        // Load existing data if this is an edit operation
        wordAdapter.wordGroupId = getIntent().getIntExtra("WORD_GROUP_ID", -1);
        if (wordAdapter.wordGroupId != -1) {
            executor.execute(() -> {
                WordGroup wordGroup = database.wordGroupDao().getWordGroupById(wordAdapter.wordGroupId);
                wordAdapter.setWords(wordGroup.words);
                wordAdapter.setWordGroupId(wordAdapter.wordGroupId);
                runOnUiThread(() -> {
                    groupNameEditText.setText(wordGroup.name);
                });
            });
        }

        // Button listeners
        saveButton.setOnClickListener(v -> saveGroup());
        addWordButton.setOnClickListener(v -> onAddWordClicked());
        addBulkButton.setOnClickListener(v -> onAddBulkClicked());
        switchAllButton.setOnClickListener(v -> onSwitchAllClicked());
    }

    private void saveGroup() {
        String groupName = groupNameEditText.getText().toString();
        if (groupName.isEmpty()) {
            Toast.makeText(this, "Group name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        WordGroup newWordGroup = new WordGroup(groupName, wordAdapter.wordList);
        newWordGroup.id = wordAdapter.wordGroupId == -1 ? newWordGroup.id : wordAdapter.wordGroupId;

        // 2. Save the WordGroup to the database
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Use your database DAO here to insert the WordGroup
                long insertWorked = wordGroupDao.insert(newWordGroup);

                //check if saved
                if ( insertWorked == -1 ) {
                    Log.d("GroupActivity", "Failed to insert group");
                }
                List<WordGroup> allGroups = wordGroupDao.getAllWordGroupsStatic();
                for (WordGroup group : allGroups) {
                    Log.d("GroupActivity", "Saved Group: " + group.name);
                }

                // Once saved, go back to the previous activity
                finish();  // This will close the current GroupActivity and go back to MainActivity
            }
        }).start();

        finish();
    }

    @Override
    public void onWordClick(int position) {
        // Handle the word click event here
        // You can get the clicked word object using the position parameter
        Word clickedWord = wordAdapter.wordList.get(position);

        // For instance, to show a toast with the clicked word
        Toast.makeText(this, "Clicked word: " + clickedWord.translation, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    public void onAddWordClicked() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_word, null);
        builder.setView(dialogView);

        EditText wordToLearnEditText = dialogView.findViewById(R.id.wordToLearnEditText);
        EditText translationEditText = dialogView.findViewById(R.id.translationEditText);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);
        Button saveButton = dialogView.findViewById(R.id.saveButton);

        AlertDialog dialog = builder.create();

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        saveButton.setOnClickListener(v -> {
            String wordToLearn = wordToLearnEditText.getText().toString().trim();
            String translation = translationEditText.getText().toString().trim();

            if (!wordToLearn.isEmpty() && !translation.isEmpty()) {
                Word newWord = new Word(wordToLearn, translation);

                if (wordAdapter.wordGroupId == -1) {
                    // For a new group, just add the word to the wordsList
                    wordAdapter.wordList.add(newWord);
                    wordAdapter.notifyDataSetChanged(); // Notify the adapter about the dataset change
                    dialog.dismiss();
                } else {
                    executor.execute(() -> {
                        WordGroup wordGroup = wordGroupDao.getWordGroupById(wordAdapter.wordGroupId);
                        if(wordGroup != null) {
                            wordGroup.words.add(newWord);
                            wordGroupDao.update(wordGroup);
                            runOnUiThread(() -> {
                                wordAdapter.setWords(wordGroup.words);
                            });
                        }
                        dialog.dismiss();
                    });
                }
            } else {
                Toast.makeText(GroupActivity.this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    public void onAddBulkClicked() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_bulk, null);
        builder.setView(dialogView);

        EditText bulkWordsEditText = dialogView.findViewById(R.id.bulkWordsEditText);
        Button bulkCancelButton = dialogView.findViewById(R.id.bulkCancelButton);
        Button bulkSaveButton = dialogView.findViewById(R.id.bulkSaveButton);

        AlertDialog dialog = builder.create();

        bulkCancelButton.setOnClickListener(v -> dialog.dismiss());

        bulkSaveButton.setOnClickListener(v -> {
            String[] lines = bulkWordsEditText.getText().toString().split("\\n");
            if (lines.length % 2 != 0) {
                Toast.makeText(GroupActivity.this, "Incomplete word-translation pairs", Toast.LENGTH_SHORT).show();
                return;
            }

            for (int i = 0; i < lines.length; i += 2) {
                String wordToLearn = lines[i].trim();
                String translation = lines[i + 1].trim();
                wordAdapter.wordList.add(new Word(wordToLearn, translation));
            }

            wordAdapter.notifyDataSetChanged();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void onBackButtonPressed() {
        String groupName = groupNameEditText.getText().toString().trim();
        if (groupName.isEmpty() && wordAdapter.wordList.isEmpty()) {
            // If both name and words list are empty, simply go back
            finish();
        } else {
            // Otherwise, save the group and then go back
            saveGroup();
        }
    }

    private void onSwitchAllClicked() {
        if (wordAdapter.wordGroupId == -1) {
            // For a new group, just swap all the words in word list wordsList
            for (Word word: wordAdapter.wordList) {
                //swap word
                String oldTranslation = word.translation;
                word.translation = word.wordToLearn;
                word.wordToLearn = oldTranslation;
            }
            wordAdapter.notifyDataSetChanged(); // Notify the adapter about the dataset change
        } else {
            executor.execute(() -> {
                WordGroup wordGroup = wordGroupDao.getWordGroupById(wordAdapter.wordGroupId);
                if(wordGroup != null) {
                    for (Word word: wordGroup.words) {
                        //swap word
                        String oldTranslation = word.translation;
                        word.translation = word.wordToLearn;
                        word.wordToLearn = oldTranslation;
                    }
                    //update
                    wordGroupDao.update(wordGroup);
                    runOnUiThread(() -> {
                        wordAdapter.setWords(wordGroup.words);
                    });
                }
            });
        }
    }
}
