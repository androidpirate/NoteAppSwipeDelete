package com.example.android.noteappswipedelete;

import android.content.DialogInterface;
import android.content.Intent;

import com.example.android.noteappswipedelete.adapter.NoteAdapter;
import com.example.android.noteappswipedelete.db.NoteDbHelper;
import com.example.android.noteappswipedelete.model.Note;
import com.example.android.noteappswipedelete.utils.RecyclerItemTouchHelper;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity
    implements NoteAdapter.NoteClickListener,
        RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {
    private NoteDbHelper mDbHelper;
    private TextView emptyListMessage;
    private RecyclerView recyclerView;
    private NoteAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDbHelper = NoteDbHelper.getInstance(this);
        // Get all notes from database
        List<Note> notes = mDbHelper.getAllNotes();
        recyclerView = findViewById(R.id.rv_note_list);
        emptyListMessage = findViewById(R.id.tv_empty_list);
        if(notes.size() == 0) {
            displayEmptyListMessage();
        } else {
            displayRecyclerView();
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            // Set adapter
            mAdapter = new NoteAdapter(notes, this);
            recyclerView.setAdapter(mAdapter);
            // Attach ItemTouchHelper callback to RecyclerView
            ItemTouchHelper.SimpleCallback itemTouchHelperCallback =
                    new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
            new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);
        }
    }

    private void displayRecyclerView() {
        emptyListMessage.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void displayEmptyListMessage() {
        recyclerView.setVisibility(View.INVISIBLE);
        emptyListMessage.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_note_action:
                // Start EditActivity
                Intent intent = new Intent(this, EditActivity.class);
                intent.putExtra(EditActivity.EXTRA_NOTE, new Note());
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(Note note) {
        // Create an intent to start DetailActivity
        Intent intent = new Intent(this, DetailActivity.class);
        // Add note as an intent extra
        intent.putExtra(DetailActivity.EXTRA_NOTE, note);
        startActivity(intent);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder holder, int direction, final int position) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.alert_title))
                .setMessage(getString(R.string.alert_message))
                .setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAdapter.restoreItem(position);
                    }
                })
                .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Remove item from database
                        mDbHelper.deleteNote(mAdapter.getNoteInPosition(position));
                        mAdapter.removeItem(position);
                        if(mAdapter.getItemCount() == 0) {
                            displayEmptyListMessage();
                        }
                    }
                }).create().show();
    }
}