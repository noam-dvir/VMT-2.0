package com.andy.vmt_app;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WordActivity extends AppCompatActivity {

    private TextView tvWordToLearn;
    private TextView tvTranslation;
    private Button btnRight;
    private Button btnWrong;
    private Button backToMenuButton;

    private List<Word> words;
    private int currentIndex = 0;
    private List<Word> wrongWords = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word);

        tvWordToLearn = findViewById(R.id.tvWordToLearn);
        tvTranslation = findViewById(R.id.tvTranslation);
        btnRight = findViewById(R.id.btnRight);
        btnWrong = findViewById(R.id.btnWrong);
        backToMenuButton = findViewById(R.id.backToMenuButton);
        // "Back to menu" button returns to Main Activity
        backToMenuButton.setOnClickListener(v -> finish());

        // get words to practice
        words = (List<Word>) getIntent().getSerializableExtra("wordList");
        //shuffle words
        Collections.shuffle(words);
        displayWord();
        //show answer
        tvTranslation.setOnClickListener(v -> tvWordToLearn.setVisibility(View.VISIBLE));
        // Right/Wrong buttons
        btnRight.setOnClickListener(v -> nextWord());
        btnWrong.setOnClickListener(v -> {
            wrongWords.add(words.get(currentIndex));
            nextWord();
        });
    }

    private void displayWord() {
        if (currentIndex < words.size()) {
            Word currentWord = words.get(currentIndex);
            tvWordToLearn.setText(currentWord.wordToLearn);
            tvTranslation.setText(currentWord.translation);
            tvWordToLearn.setVisibility(View.GONE); // Hide the wordToLearn initially
            tvTranslation.setVisibility(View.VISIBLE); // Make the translation visible
        } else {
            handleIterationEnd();
        }
    }


    private void handleIterationEnd() {
        if (!wrongWords.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Next Iteration")
                    .setMessage("Starting new iteration with " + wrongWords.size() + " words")
                    .setPositiveButton("OK", (dialog, which) -> {
                        words = new ArrayList<>(wrongWords); // copy wrong words to main list
                        wrongWords.clear();
                        currentIndex = 0;
                        Collections.shuffle(words);
                        displayWord();
                    })
                    .setCancelable(false)
                    .show();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Finished!")
                    .setMessage("Well done!")
                    .setPositiveButton("Back to menu", (dialog, which) -> finish())
                    .setCancelable(false)
                    .show();
        }
    }

    private void nextWord() {
        currentIndex++;
        displayWord();
    }
}
