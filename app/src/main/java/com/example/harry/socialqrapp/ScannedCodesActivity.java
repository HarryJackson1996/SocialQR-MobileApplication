package com.example.harry.socialqrapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Activity for displaying List of codes the current user has scanned.
 * Displays 'Friends List'.
 */
public class ScannedCodesActivity extends AppCompatActivity {

    //Initialisation of widgets.
    private GridLayout gridMain;
    private ListView listView;

    //Initialisation of Java fields.
    private ArrayList<String> array, array1;

    //Initialisation of Firebase fields.
    private FirebaseAuth mAuth;

    /**
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanned_codes);

        // Creating objects:
        gridMain = (GridLayout) findViewById(R.id.gridMain);
        setGridMenu(gridMain);

        listView = findViewById(R.id.ListViewUrl);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        array = new ArrayList<String>();

        mAuth = FirebaseAuth.getInstance();
        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        final DatabaseReference usersdRef = rootRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("friends");

        //This handles displaying the users friends in a Listview.
        //We firstly for reach through the current database reference to get all children (friends).
        usersdRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                for (final DataSnapshot ds : dataSnapshot.getChildren()) {
                    array.add(ds.getKey());
                    final ArrayAdapter<String> adapter = new ArrayAdapter(ScannedCodesActivity.this, R.layout.custom_listview, array);
                    listView.setAdapter(adapter);

                    //Set onClickListeners for each item (friend) in list view.
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            final SparseBooleanArray checked = listView.getCheckedItemPositions();
                            final ArrayList<String> selectedItems = new ArrayList<String>();

                            // We check which item in the list has been clicked.
                            for (int i = 0; i < checked.size(); i++) {
                                position = checked.keyAt(i);
                                final int pos = position;
                                if (checked.valueAt(i)) {
                                    selectedItems.add(adapter.getItem(pos)); //get the position of the item selected
                                    String username = selectedItems.get(i);  //get the username (first + surname) of the item selected
                                    DatabaseReference mRef = usersdRef.child(username); //search the database for the friends username.
                                    array1 = new ArrayList<String>();
                                    mRef.addChildEventListener(new ChildEventListener() {  //Add eventListener
                                        @Override
                                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                            String value = dataSnapshot.getValue(String.class); //get all the information associated with that friend
                                            array1.add(value);                                  //add the value to a new array
                                            String friendInfo = array1.toString();              //convert array to string
                                            Intent intent = new Intent(ScannedCodesActivity.this, FriendUrlActivity.class);
                                            intent.putExtra("friendInfo", friendInfo);    //Pass the string into the FriendUrlActivity to be displayed/
                                            startActivity(intent);
                                        }
                                        @Override
                                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                                        }

                                        @Override
                                        public void onChildRemoved(DataSnapshot dataSnapshot) {

                                        }

                                        @Override
                                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }


                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    /**
     * Method for displaying the cardView.
     * Similar functionality to a Button however visually different (for UX/ consistency across application).
     * Sets onClick for the single button allowing the user to return home
     *
     * @param gridMain the ViewGroup to display the cardView.
     */
    private void setGridMenu(GridLayout gridMain) {
        for (int i = 0; i < gridMain.getChildCount(); i++) {
            CardView cardView = (CardView) gridMain.getChildAt(i);
            final int cardNumber = i;
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (cardNumber == 0) {
                        if(Utility.devMode()==true) {
                            setToastMessage("Return home button clicked");
                        }
                        Intent intent = new Intent(ScannedCodesActivity.this, HomeScreenActivity.class);
                        startActivity(intent);
                    }
                }
            });
        }
    }

    /**
     * Method for passing Toast messages
     *
     * @param message to be displayed
     */
    private void setToastMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}
