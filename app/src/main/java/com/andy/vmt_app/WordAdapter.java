package com.andy.vmt_app;

import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class WordAdapter extends RecyclerView.Adapter<WordAdapter.WordViewHolder> {

    private List<Word> wordList;
    private int currentWordGroupId;
    private OnWordClickListener onWordClickListener;

    public WordAdapter(List<Word> wordList, OnWordClickListener onWordClickListener) {
        this.wordList = wordList;
        this.onWordClickListener = onWordClickListener;
    }

    @NonNull
    @Override
    public WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_word, parent, false);
        return new WordViewHolder(itemView, onWordClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
        Word currentWord = wordList.get(position);
        holder.wordToLearnTextView.setText(currentWord.wordToLearn);
        holder.translationTextView.setText(currentWord.translation);
    }

    @Override
    public int getItemCount() {
        return wordList.size();
    }

    public void setWords(List<Word> words) {
        this.wordList = words;
        notifyDataSetChanged();
    }
    
    

    public void setCurrentWordGroupId(int wordGroupId) {
        this.currentWordGroupId = wordGroupId;
    }


    public class WordViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView wordToLearnTextView;
        private TextView translationTextView;
        private ImageButton deleteWordButton;
        public ImageButton editWordButton;
        private OnWordClickListener onWordClickListener;
        private ImageButton switchButton;

        public WordViewHolder(@NonNull View itemView, OnWordClickListener onWordClickListener) {
            super(itemView);
            wordToLearnTextView = itemView.findViewById(R.id.word_to_learn_text_view);
            translationTextView = itemView.findViewById(R.id.translation_text_view);
            deleteWordButton = itemView.findViewById(R.id.delete_word_button);
            editWordButton = itemView.findViewById(R.id.editWordButton);
            switchButton = itemView.findViewById(R.id.buttonSwitch);
            this.onWordClickListener = onWordClickListener;

            deleteWordButton.setOnClickListener(this);
            deleteWordButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Word wordToDelete = wordList.get(getAdapterPosition());
                    AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
                    builder.setTitle("Delete Word")
                            .setMessage( String.format("Are you sure you want to delete this word?\n%s\n%s",
                                    wordToDelete.wordToLearn, wordToDelete.translation) )
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    int indexToRemove = getBindingAdapterPosition();
                                    deleteWord(indexToRemove);
                                }
                            })
                            .setNegativeButton("No", null)
                            .show();
                }
            });

            editWordButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getBindingAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Word wordToEdit = wordList.get(position);

                        //setup dialog
                        LayoutInflater inflater = LayoutInflater.from(itemView.getContext());
                        View dialogView = inflater.inflate(R.layout.dialog_add_word, null);
                        AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
                        builder.setView(dialogView);
                        AlertDialog alertDialog = builder.create();

                        //text inputs
                        final EditText wordInput = dialogView.findViewById(R.id.wordToLearnEditText);
                        final EditText translationInput = dialogView.findViewById(R.id.translationEditText);

                        wordInput.setText(wordToEdit.wordToLearn);
                        translationInput.setText(wordToEdit.translation);

                        //Define button's logic
                        Button saveButton = dialogView.findViewById(R.id.saveButton);
                        Button cancelButton = dialogView.findViewById(R.id.cancelButton);

                        saveButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // get new values
                                String updatedWord = wordInput.getText().toString();
                                String updatedTranslation = translationInput.getText().toString();

                                wordToEdit.wordToLearn = updatedWord;
                                wordToEdit.translation = updatedTranslation;

                                Executor executor = Executors.newSingleThreadExecutor();
                                executor.execute(() -> {
                                    //update the group
                                    WordGroup wordGroup = YourDatabaseClass.getInstance(itemView.getContext()).wordGroupDao().getWordGroupById(currentWordGroupId);
                                    wordGroup.words.get(position).wordToLearn = updatedWord;
                                    wordGroup.words.get(position).translation = updatedTranslation;
                                    YourDatabaseClass.getInstance(itemView.getContext()).wordGroupDao().update(wordGroup);

                                    new Handler(Looper.getMainLooper()).post(() -> {
                                        //update the adapter's wordList
                                        wordList.get(position).wordToLearn = updatedWord;
                                        wordList.get(position).translation = updatedTranslation;
                                        notifyDataSetChanged();
                                    });
                                });

                                alertDialog.dismiss();
                            }
                        });

                        cancelButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // Simply dismiss the dialog
                                alertDialog.dismiss();
                            }
                        });

                        alertDialog.show();
                    }
                }
            });

            switchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getBindingAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Executor executor = Executors.newSingleThreadExecutor();
                        executor.execute(() -> {
                            //get existing values
                            WordGroup wordGroup = YourDatabaseClass.getInstance(itemView.getContext()).wordGroupDao().getWordGroupById(currentWordGroupId);
                            String oldWordToLearn = new String(wordGroup.words.get(position).wordToLearn);
                            String oldTranslation = new String(wordGroup.words.get(position).translation);
                            //update word on database
                            wordGroup.words.get(position).wordToLearn = oldTranslation;
                            wordGroup.words.get(position).translation = oldWordToLearn;
                            YourDatabaseClass.getInstance(itemView.getContext()).wordGroupDao().update(wordGroup);
                            //update wordList
                            new Handler(Looper.getMainLooper()).post(() -> {
                                //update the adapter's wordList
                                wordList.get(position).wordToLearn = oldTranslation;
                                wordList.get(position).translation = oldWordToLearn;
                                notifyDataSetChanged();
                            });
                        });
                    }
                }
            });
        }

        public void deleteWord(int indexToRemove) {
            Executor executor = Executors.newSingleThreadExecutor();
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    // Fetch the word group
                    WordGroup wordGroup = YourDatabaseClass.getInstance(itemView.getContext()).wordGroupDao().getWordGroupById(currentWordGroupId); // Assuming you have wordGroupId available

                    // Remove the word from the group
                    wordGroup.words.remove(indexToRemove);

                    // Update the word group in the database
                    YourDatabaseClass.getInstance(itemView.getContext()).wordGroupDao().update(wordGroup);

                    new Handler(Looper.getMainLooper()).post(() -> {
                        // Update your adapter's data and notify the RecyclerView
                        wordList.remove(indexToRemove);
                        notifyDataSetChanged();
                    });

                }
            });
        }


        private void updateWordGroupInDatabase(final WordGroup wordGroup) {
            Executor executor = Executors.newSingleThreadExecutor();
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    YourDatabaseClass.getInstance(itemView.getContext()).wordGroupDao().update(wordGroup);
                }
            });
        }

        @Override
        public void onClick(View v) {
            onWordClickListener.onWordClick(getAdapterPosition());
        }
    }

    public interface OnWordClickListener {
        void onWordClick(int position);
    }
}

