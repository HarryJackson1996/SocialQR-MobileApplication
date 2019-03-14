package com.example.harry.socialqrapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.sql.Array;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Scan Result Activity for displaying the result of the QR code.
 */
public class ScanResultActivity extends AppCompatActivity {

    //Initialisation of widgets.
    private ArrayAdapter<String> adapter;
    private ListView listView;
    private GridLayout gridMain;

    //Initialisation of Java fields.
    public List<String> newArray;
    public ArrayList<String> urlList;

    //Initialisation of Firebase fields.
    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;

    /**
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_result);

        // Create objects.
        urlList = new ArrayList<String>();
        listView = (ListView) findViewById(R.id.ListView);
        gridMain = (GridLayout) findViewById(R.id.gridMain);
        setGridMenu(gridMain);

        // Retrieve the text send from the Scanned QR code
        // As the text is a long String of text we need to chop it up back into the relevant URL's
        // We call the local method Change() to handle to this conversion.
        // Create new Parent in the Firebase Tree structure to contain the scanned URL's
        Intent intent = getIntent();
        final String result = intent.getExtras().getString("infoQR");
        Change(result);
        String username = newArray.get(0).toString();
        mAuth = FirebaseAuth.getInstance();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid()).child("friends").child(username);
    }

    /**
     * Method uses regular Expressions to split the String up after each comma (,).
     * We create a new List for each item split from the String result.
     * We display the URL's into a ListView to display the shared information.
     *
     * @param result the result retrieved from the QR code
     */
    public void Change(String result) {

        if (result.contains("[") || result.contains("]")) {
            result = result.replace("]", "");
            result = result.replace("[", "");

            if(Utility.devMode()==true) {
                Toast.makeText(ScanResultActivity.this, result, Toast.LENGTH_SHORT).show();
            }

            newArray = Arrays.asList(result.split(","));

            adapter = new ArrayAdapter<String>(ScanResultActivity.this, R.layout.custom_listview, newArray);
            listView.setAdapter(adapter);
            adapter.notifyDataSetChanged();

        }
    }

    /**
     * Method for clicking item in the cardView Menu.
     * implements functionality for each item in menu.
     * There are two buttons:
     * The first button saves the scanned information to the user (who is scanning) database.
     * New FireBase Tree Structure:
     *  * Users (root)
     *      -> UID (parent)
     *            -> URL       (value)
     *            -> firstname (value)
     *            -> surname   (value)
     *            -> userID    (value)
     *            -> URLS      (parent)
     *                 -> url1 (value)
     *                 -> url2 (value)
     *            -> friends   (parent)
     *                 -> friend1 (value)
     *                      -> url1 (value)
     *                      -> url2 (value)
     * The second button allows the user to return home and delete the scanned information.
     * The second buttons implementation is required as people may be pressured into scanning
     * another users QR code, this allows them to just disregard the information retrieved.
     *
     * @param gridMain
     */
    private void setGridMenu(GridLayout gridMain) {
        for (int i = 0; i < gridMain.getChildCount(); i++) {
            CardView cardView = (CardView) gridMain.getChildAt(i);
            final int cardNumber = i;
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (cardNumber == 0) {
                        int i = 1;
                        for(String urls: newArray){
                            String Url = urls.toString();
                            mUserDatabase.child("url" + i).setValue(Url);
                            i++;
                        }
                        if(Utility.devMode() == true) {
                            setToastMessage("Save button clicked");
                        }
                        Intent intent = new Intent(ScanResultActivity.this, HomeScreenActivity.class);
                        startActivity(intent);
                    }

                    if (cardNumber == 1) {
                        if(Utility.devMode() == true ) {
                            setToastMessage("Return home button clicked");
                        }
                        Intent intent = new Intent(ScanResultActivity.this, HomeScreenActivity.class);
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
