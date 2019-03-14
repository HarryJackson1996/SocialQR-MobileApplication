package com.example.harry.socialqrapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.tooltip.Tooltip;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

/**
 * Generate QR code Activity for generating QR codes in realtime from the URLs saved
 * in the current users Database.
 * Handles the creation of a QR code.
 */
public class GenerateQRActivity extends AppCompatActivity {

    //Initialisation of widgets.
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private GridLayout gridMain;
    private Button btnGenerate;
    private Tooltip tooltip;

    //Initialisation of Java fields.
    public int counter = 0;
    private ArrayList<String> urlList;

    //Initialisation of Firebase fields.
    private FirebaseAuth mAuth;

    /**
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_qr);

        // Creating objects:
        urlList = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(GenerateQRActivity.this, android.R.layout.simple_list_item_multiple_choice, urlList);

        listView = (ListView) findViewById(R.id.ListViewUrl);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        gridMain = (GridLayout) findViewById(R.id.gridMain);
        setGridMenu(gridMain);

        mAuth = FirebaseAuth.getInstance();

        // Database reference to current users instance and retrieving user Urls.
        // 'URLS' is the table of current users
        //  We get current user by using mAuth.getCurrentUser().getUid()
        // And retrieve the child in the realtime database to retrieve users Urls.
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("Users");
        final DatabaseReference mRef1 = mRef.child(mAuth.getCurrentUser().getUid());
        DatabaseReference mRef2 = mRef1.child("URLS");

        // Creates eventListener to retrieve information from real-time database
        // Currently retrieves URL's from current Users database and adds it to ArrayList.
        mRef2.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String value = dataSnapshot.getValue(String.class);
                urlList.add(value);
                adapter.notifyDataSetChanged();
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

        // We set a onItemClickListener to the ListView
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // Adds check boxes to each item in the ListView.
                // Create new ArrayList<String> selectedItems to add all selected items.
                final SparseBooleanArray checked = listView.getCheckedItemPositions();
                final ArrayList<String> selectedItems = new ArrayList<String>();


                // Retrieves the current users first and last name for display purposes.
                mRef1.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null) {
                            String name = dataSnapshot.child("firstname").getValue().toString();
                            String surname = dataSnapshot.child("surname").getValue().toString();

                            selectedItems.add(0, name + " " + surname);

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                // Iterates through spareseBooleanArray
                for (int i = 0; i < checked.size(); i++) {
                    position = checked.keyAt(i);
                    final int lol = position;
                    if (checked.valueAt(i)) {
                        selectedItems.add(adapter.getItem(lol));
                    }
                }

                // Add OnClickListener to button (Generate).
                findViewById(R.id.btnGenerate).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // Iterate through String ArrayList
                        for (String s : selectedItems) {

                            String list = selectedItems.toString();

                            // So if selectedItems in the ListView is greater then 0.
                            // All items selected in the ListView have been added to the ArrayList selectedItems.
                            // We encode the URLs from the ArrayList into a QR code.
                            // We convert the QR code into a new ByteArrayOutputStream to create a buffer in memory.
                            // Compress the bitmap into PNG format (image)
                            // We use putExtra which allows us to move/display the compressed Image into another Activity.
                            // The activity it is sent to is the QRCheckActivity.
                            if (selectedItems.size() != 0) {
                                MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                                try {
                                    BitMatrix bitMatrix = multiFormatWriter.encode(list, BarcodeFormat.QR_CODE, 500, 500);
                                    BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                                    Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                                    Intent i = new Intent(GenerateQRActivity.this, QRCheckActivity.class);
                                    ByteArrayOutputStream bs = new ByteArrayOutputStream();
                                    bitmap.compress(Bitmap.CompressFormat.PNG, 50, bs);
                                    i.putExtra("byteArray", bs.toByteArray());
                                    startActivity(i);
                                } catch (WriterException e) {
                                    e.printStackTrace();
                                }
                            }
                            if(Utility.devMode() == true) {
                                Toast.makeText(GenerateQRActivity.this, "Array" + selectedItems.toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }
        });
    }

    /**
     * Method for displaying the cardView.
     * Similar functionality to a Button however visually different (for UX/ consistency across application).
     * Sets onClick for each Card (Button).
     *
     * @param gridMain the ViewGroup to display the cardView.
     */
    private void setGridMenu(final GridLayout gridMain) {
        for (int i = 0; i < gridMain.getChildCount(); i++) {
            final CardView cardView = (CardView) gridMain.getChildAt(i);
            final int cardNumber = i;
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (cardNumber == 0) {
                        if(Utility.devMode()==true) {
                            setToastMessage("Return home button clicked");
                        }
                        Intent intent = new Intent(GenerateQRActivity.this, HomeScreenActivity.class);
                        startActivity(intent);
                    }
                    if (cardNumber == 2) {
                        if(counter%2==0) {
                            tooltip = new Tooltip.Builder(gridMain)
                                    .setText("Click the items you wish to encode, then click Generate to create your QR code!")
                                    .setGravity(Gravity.BOTTOM)
                                    .show();
                            counter++;
                        }
                        else if(counter%2==1){
                            tooltip.dismiss();
                            counter++;
                        }
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
