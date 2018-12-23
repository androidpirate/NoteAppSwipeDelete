# Previously on Building A Note Taking App

In the [**previous tutorial**](https://androidpirate.github.io/NoteAppSqliteInsert/ "**previous tutorial**"), we implemented **insertNote()** method to insert new notes
into database and created **EditActivity** to display a user interface to add/edit notes. We also learned how to use **AppBar** to display
action items.



# NoteApp Swipe to Delete – Tutorial 6

Start by cloning/forking the app from the link below:

[**NoteApp Swipe to Delete - Tutorial 6**](https://github.com/androidpirate/NoteAppSwipeDelete "**NoteApp Swipe to Delete - Tutorial 6**")



### Goal of This Tutorial

The goal of this tutorial is to implement a new method in **NoteDbHelper** to delete notes in the database. We are also going to
implement a swipe to delete function in the **RecyclerView**, which will display an **Alert Dialog** to confirm or cancel the action.

An **Alert Dialog** is a specific dialog, that can show a title, up to three buttons, a list of selectable items or a custom layout.
It is a best practice to **ONLY** display dialogs that prompts the user to make a decision on a crucial task, such as deleting an item from
the database. Due to their interrupting nature **(..in your face! attitude)**, they should not be used to display feedback. [**Toast Messages**](https://developer.android.com/guide/topics/ui/notifiers/toasts "**Toast Messages**") and [**Snack Bar**](https://developer.android.com/reference/android/support/design/widget/Snackbar "**Snack Bars**") on the other hand are great ways to
display feedback, and they do it without interrupting the user.



### What’s in Starter Module?

Starter module already has a fully functional **RecyclerView** that works with a **SQLite database** to get a list of notes. The **App Bar**
provides **ADD** action to allow the user to add new notes to the database.

You can follow the steps below and give it a shot yourself, and if you stuck at some point, check out the **solution module** or the rest
of the tutorial.



### Steps to Build

1. Implement **deleteNote()** method in **NoteDbHelper**
2. Add a new method in **NoteAdapter**: getNoteInPosition()
3. Add a new method in **NoteAdapter**: removeItem()
4. Add a new method in **NoteAdapter**: restoreItem()
5. Add a **FrameLayout** as the main container in **list_item.xml**
6. Add a new **RelativeLayout** as a sibling to existing **CardView** element in **list_item.xml**, make sure to give it an id
7. Add a **TextView** element as first child of **RelativeLayout**
8. Add a **ImageView** element as second child of **RelativeLayout**
9. Change **NoteHolder**'s access modifier from **private** to **public**
10. Add a new field in **NoteHolder class**: foreground_view
11. Implement an **ItemTouchHelper** class for **RecyclerView**
12. Attach **ItemTouchHelper** to  **RecyclerView** in **MainActivity**
13. Implement **onSwipe()** method in **MainActivity**



### Deleting Notes from Database

There is only one source of truth in **NoteApp** and that is the **Note database**, if we can insert data so we shall delete:


```java
public class NoteDbHelper extends SQLiteOpenHelper {
  // Fields and callbacks are excluded for simplicity
  public void deleteNote(Note note) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(NoteContract.NoteEntry.TABLE_NAME,
                       NoteContract.NoteEntry._ID + " = ?",
                       new String[]{String.valueOf(note.get_id())});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Problem deleting note from database.");
        } finally {
            db.endTransaction();
        }
    }
}
```


We implement a new **deleteNote()** method which gets a **writable** instance of **Note database** and starts a transaction to delete the
**Note** passed as an argument. In order to perform a delete operation we need to specify which item in the table we would like to delete.
 **db.delete()** method accepts three arguments, **Table Name**, **Where Clause** and **Where Arguments**. **Where Clause** specifies which
 column we are referring to. In this case, we are referring to **_ID column**, and **Where Arguments** replaces the **"?"**
 in **Where Clause**. (More info about [**SQLiteDatabase Delete**](https://developer.android.com/reference/android/database/sqlite/SQLiteDatabase#delete(java.lang.String,%20java.lang.String,%20java.lang.String%5B%5D "**SQLiteDatabase Delete**"))



### Removing Items from RecyclerView

It is all good and fair to delete items from the database. But the user does not interact with the database directly! The UI component that
 displays the data is **RecyclerView** and the organizer of the data is **NoteAdapter**.

The problem is as we swipe notes from the list to delete them, **NoteAdapter**'s list of notes doesn't refresh automatically. We need a way
 to delete notes from the list, so we can simply implement a method in **NoteAdaper** to delete a note:


```java
public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteHolder> {
  // Fields and callbacks are excluded for simplicity
  .
  .
  public void removeItem(int position) {
          notes.remove(position);
          // Do not call notifyDataSetChange()
          // instead use notifyItemRemoved(position)
          notifyItemRemoved(position);
  }
  .
  .
}
```


We pass the **adapter position** of the note **(list item position)** to **removeItem() method** and remove the item in position, and
then we notify **NoteAdapter** about the item removed. The key is not to use **notifyDataSetChange()**, which checks the entire dataset and it is a costly operation, instead, we use **notifyItemRemoved()** simply let adapter know which item is removed.  

**(What if I change my mind about removing an item???)** Good question! **(Because making something disappear is not enough...You have to
bring it back.)** But we are not going to implement an **Undo** button to restore an item though. Instead, we are going to pop a dialog to
ask the user one last time about their decision and it will be final:


```java
public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteHolder> {
  // Fields and callbacks are excluded for simplicity
  .
  .
  public void restoreItem(int position) {
        notifyItemChanged(position);
    }
  .
  .
}
```


All **restoreItem() method** does is to refresh the page so the swiped item will return back to the list as it was before.



### Swipe to Delete

As the name indicates, we are implementing a function that uses a swipe action. So we need to display an animation that feels like the user
is swiping notes in **RecyclerView** to delete them. So we start with editing **list_item.xml**. We need to add a **FrameLayout** that will
be act as a container for **background** and **CardView**. Also, we need to give **CardView** an id of **cv_foreground** which we will use
to get a reference in the **NoteHolder class** next:


```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical">

    <RelativeLayout
        android:id="@+id/rl_background"
        android:layout_width="match_parent"
        android:layout_height="110dp"
        android:layout_margin="4dp">

        <TextView
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:text="@string/delete"
            android:textSize="18sp"
            android:textStyle="bold"
            android:textColor="@color/color_red"
            android:gravity="center"/>

        <ImageView
            android:id="@+id/iv_delete"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:src="@drawable/ic_delete"
            android:layout_centerInParent="true"
            android:layout_alignParentEnd="true"
            android:layout_marginEnd="32dp"
            tools:ignore="contentDescription"/>
    </RelativeLayout>

    <!-- CardView goes here -->
    <android.support.v7.widget.CardView
        android:id="@+id/cv_foreground"
        android:layout_width="match_parent"
        android:layout_height="110dp"
        app:cardBackgroundColor="@android:color/holo_orange_light"
        app:cardElevation="5dp"
        app:cardCornerRadius="5dp"
        android:layout_margin="8dp">
    <!-- Other UI elements are excluded for simplicity -->
</FrameLayout>
```


As seen above, we added a **RelativeLayout** as a background for the **CardView** which has a **TextView** and **ImageView** elements in it.
The background will be revealed as the user swipes the **CardView**.


Switch back to **NoteAdaper class** and change its access modifier to **public**. Also, add a new field for **CardView** as
**foreground_view** and get a reference to it:


```java
.
.
public class NoteHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private TextView description;
        public CardView foregroundView;

      // Fields and callbacks are excluded for simplicity
      public NoteHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_title);
            description = itemView.findViewById(R.id.tv_description);
            foregroundView = itemView.findViewById(R.id.cv_foreground);
            // Call NoteClickListener's onClick when an item is clicked
          .
          .
        }
}
```



### ItemTouchHelper

**ItemTouchHelper** is a utility class which adds swipe to dismiss and drag and drop support to **RecyclerView** (Check out the official
  documentation [**here**](https://developer.android.com/reference/android/support/v7/widget/helper/ItemTouchHelper.SimpleCallback "**here**"))

Add a new package called **utils**. Under **utils package** add a new class called **RecyclerItemTouchHelper** which extends **ItemTouchHelper.SimpleCallback**:


```java
public class RecyclerItemTouchHelper extends ItemTouchHelper.SimpleCallback {
    private RecyclerItemTouchHelperListener listener;

    public interface RecyclerItemTouchHelperListener {
        void onSwiped(RecyclerView.ViewHolder holder, int direction, int position);
    }

    public RecyclerItemTouchHelper(int dragDirs, int swipeDirs, RecyclerItemTouchHelperListener listener) {
        super(dragDirs, swipeDirs);
        this.listener = listener;
    }
}
```


First, we create a listener interface called **RecyclerItemTouchHelperListener** to listen to swipe action. Then we simply add a private
field for the listener itself. In the constructor, we call the **super implementation** and assign the listener that we passed as an argument.

There are bunch of callbacks required to be implemented:
* onMove()
* OnSelectedChanged()
* clearView()
* onChildDraw()
* onSwiped():


```java
public class RecyclerItemTouchHelper extends ItemTouchHelper.SimpleCallback {
  .
  .
  @Override
  public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
      // Is used to support drag and drop action, return false to not to support
      return false;
  }

  @Override
  public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
      // Called when the ViewHolder swiped or dragged by ItemTouchHelper is changed
      if(viewHolder != null) {
          final View foregroundView = ((NoteAdapter.NoteHolder) viewHolder).foregroundView;
          getDefaultUIUtil().onSelected(foregroundView);
      }
  }

  @Override
  public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
      // Clears the foregroundView from the itemView as the user interaction is completed
      final View foregroundView = ((NoteAdapter.NoteHolder) viewHolder).foregroundView;
      getDefaultUIUtil().clearView(foregroundView);
  }

  @Override
  public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
      // Draws the foregroundView(itemView) on top of the background
      final View foregroundView = ((NoteAdapter.NoteHolder) viewHolder).foregroundView;
      getDefaultUIUtil().onDraw(c, recyclerView, foregroundView, dX, dY, actionState, isCurrentlyActive);
  }

  @Override
  public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
      // Calls listener's onSwipe() method
      listener.onSwiped(viewHolder, direction, viewHolder.getAdapterPosition());
  }
}
```


Basically, **RecyclerItemTouchHelper** draws the **foregroundView** on top of the **background** for each RecyclerView item and listens for the swipe events. When the user swipes a card, it clears the **foregroundView** and displays **background**. It is time to attach it, to
our RecyclerView in **MainActivity**.



### Attaching RecyclerItemTouchHelper

Open **MainActivity** implement **RecyclerItemTouchHelperListener** and get an instance of **RecyclerItemTouchHelper** in **onCreate()**:


```java
public class MainActivity extends AppCompatActivity
    implements NoteAdapter.NoteClickListener,
        RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {
    // Fields and callbacks are excluded for simplicity
    .
    .
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        .
        .
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
    .
    .
}
```


Now we implement what happens when the user swipes a note from the list. Override **onSwipe() method** and create an alert dialog to warn the user they are about to delete a note:


```java
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
```


As seen above, when the user clicks on cancel, we are restoring the item swiped, and deleting the note permanently if they choose to delete them, both from the database and the list. And that's it!!! Run the app and swipe some of those notes.



### What's In Next Tutorial

In our next tutorial [**tutorial**](https://androidpirate.github.io/NoteAppButterknife/ "NoteAppButterknife"), we will use **ButterKnife** a popular library to inject views in **NoteApp**.



### Resources

1. [Android Developer Guides](https://developer.android.com/guide/ "Android Developer Guides") by Google
