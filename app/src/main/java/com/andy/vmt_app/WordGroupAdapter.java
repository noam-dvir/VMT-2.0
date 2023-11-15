package com.andy.vmt_app;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class WordGroupAdapter extends RecyclerView.Adapter<WordGroupAdapter.ViewHolder> {

    private List<WordGroup> wordGroups;
    private Context context;
    private OnItemClickListener listener;

    public WordGroupAdapter(List<WordGroup> wordGroups, Context context, OnItemClickListener listener1) {
        this.wordGroups = wordGroups;
        this.context = context;
        this.listener = listener1;
    }

    @NonNull
    @Override
    public WordGroupAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.word_group_item, parent, false);
        return new WordGroupAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WordGroupAdapter.ViewHolder holder, int position) {
        WordGroup wordGroup = wordGroups.get(position);
        holder.nameTextView.setText(wordGroups.get(position).name);
        holder.sizeTextView.setText("size: " + wordGroup.words.size());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(wordGroup);
                }
            }
        });
    }



    @Override
    public int getItemCount() {
        return wordGroups.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView sizeTextView;
        Button editButton;
        ImageButton deleteGroupButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.tvGroupName);
            sizeTextView = itemView.findViewById(R.id.group_size_text_view);

            //setup 'Edit Group' button
            editButton = itemView.findViewById(R.id.btnEditGroup);
            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION) { // Ensure position is valid
                        WordGroup wordGroupToEdit = wordGroups.get(position);
                        Intent intent = new Intent(context, GroupActivity.class);
                        intent.putExtra("WORD_GROUP_ID", wordGroupToEdit.id);
                        context.startActivity(intent);
                    }
                }
            });
            //setup 'Delete Group' button
            deleteGroupButton = itemView.findViewById(R.id.deleteGroupButton);
            deleteGroupButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDeleteConfirmationDialog();
                }
            });
        }

        private void showDeleteConfirmationDialog() {
            int indexToRemove = getBindingAdapterPosition();
            WordGroup currentGroup = wordGroups.get(indexToRemove);

            AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
            builder.setTitle("Delete Group");
            builder.setMessage("Are you sure you want to delete this group?\n\nGroup Name: " + currentGroup.name + "\nGroup Size: " + currentGroup.words.size());

            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Delete group from database
                    deleteGroupFromDatabase(currentGroup, indexToRemove);
                }
            });

            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }

        private void deleteGroupFromDatabase(WordGroup group, int indexToRemove) {
            Executor executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                YourDatabaseClass.getInstance(itemView.getContext()).wordGroupDao().delete(group);

                // Refresh the list on the main thread
                ((Activity) itemView.getContext()).runOnUiThread(() -> {
                    wordGroups.remove(indexToRemove);
                    notifyItemRemoved(indexToRemove);
                });
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(WordGroup wordGroup);
    }
}
