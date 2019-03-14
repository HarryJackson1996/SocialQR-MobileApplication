package com.example.harry.socialqrapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.util.Linkify;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This Class is for displaying all the data from a saved QR code.
 */
public class FriendUrlActivity extends AppCompatActivity {

    //Initialisation of widgets.
    private ArrayAdapter<String> adapter;
    private ListView listView;
    private GridLayout gridMain, gridMainTwo;

    //Initialisation of Java fields.
    private TextView friendNameText;
    public List<String> newArray;
    public ArrayList<String> urlList;
    private String getName;

    //Initialisation of Firebase fields.
    private DatabaseReference mRef;
    private FirebaseAuth mAuth;

    /**
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_url);

        friendNameText = (TextView) findViewById(R.id.friendNameText);
        listView = (ListView) findViewById(R.id.ListView);
        urlList = new ArrayList<String>();

        gridMain = (GridLayout) findViewById(R.id.gridMain);
        gridMainTwo = (GridLayout) findViewById(R.id.gridMainTwo);
        setGridMenu(gridMain);
        gridMenuTwo(gridMainTwo);

        mAuth = FirebaseAuth.getInstance();

        //Get information from previous activity (ScannedCodesActivity).
        Intent intent = getIntent();
        final String result = intent.getExtras().getString("friendInfo");
        //Call local method to change the information we recieve into a more suitable format.
        Change(result);

        final WebView webView = new WebView(this);
        //Handles opening links in the listView in the phones web browser
        //Or creating a new contact in the user phone, if a phone number is clicked.
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String Phone;
                if(position!=0) {
                    if(listView.getAdapter().getItem(position).toString().contains("07")){
                        Phone = listView.getAdapter().getItem(position).toString();
                        Intent intent = new Intent(Intent.ACTION_INSERT);
                        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
                        intent.putExtra(ContactsContract.Intents.Insert.NAME, listView.getAdapter().getItem(0).toString());
                        intent.putExtra(ContactsContract.Intents.Insert.PHONE, Phone);
                        getApplicationContext().startActivity(intent);
                    } else {
                        setContentView(webView);
                        webView.loadUrl(listView.getAdapter().getItem(position).toString());
                    }
                }
            }
        });
    }

    /**
     * Method uses regular Expressions to split the String up after each comma (,).
     * We create a new List for each item split from the String result.
     * We display the URL's into a ListView to display the information.
     *
     * @param result the result
     */
    public void Change(String result) {

        if (result.contains("[") || result.contains("]")) {
            result = result.replace("]", "");
            result = result.replace("[", "");

            newArray = Arrays.asList(result.split(","));
            getName = newArray.get(0).toString();
            friendNameText.setText(getName);
            adapter = new ArrayAdapter<String>(FriendUrlActivity.this, R.layout.custom_listview, newArray);
            listView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * Method for displaying the cardView.
     * Similar functionality to a Button however visually different (for UX/ consistency across application).
     * Sets onClick for each Card (Button).
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
                        setToastMessage("Return home button clicked");
                        Intent intent = new Intent(FriendUrlActivity.this, ScannedCodesActivity.class);
                        startActivity(intent);
                    }
                    if (cardNumber == 2) {
                        setToastMessage("Return home button clicked");
                        Intent intent = new Intent(FriendUrlActivity.this, HomeScreenActivity.class);
                        startActivity(intent);
                    }
                }
            });


        }
    }

    /**
     * Method for displaying the cardView.
     * Similar functionality to a Button however visually different (for UX/ consistency across application).
     * Sets onClick for the single Card (Delete Button).
     * If the delete Button is clicked, alert message is displayed, and if the user clicks yes
     * we delete the friend from the current users Friends List in the Firebase Database.
     *
     * @param gridMain the ViewGroup to display the cardView.
     */
    private void gridMenuTwo(GridLayout gridMain) {
        for (int i = 0; i < gridMain.getChildCount(); i++) {
            CardView cardView = (CardView) gridMain.getChildAt(i);
            final int cardNumber = i;
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (cardNumber == 0) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(FriendUrlActivity.this);
                        builder.setMessage("Are you sure you wish to delete friend?");
                        builder.setCancelable(true);
                        builder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid()).child("friends").child(getName);
                                mRef.removeValue();
                                setToastMessage("Return home button clicked");
                                Intent intent = new Intent(FriendUrlActivity.this, ScannedCodesActivity.class);
                                startActivity(intent);
                            }
                        });
                        builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();

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
