package com.example.harry.socialqrapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.GridLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tooltip.Tooltip;

import java.util.ArrayList;

/**
 * Url Activity for saving URL's during profile setup to the users current database.
 */
public class UrlActivity extends AppCompatActivity {

    //Initialisation of widgets.
    private Tooltip tooltip;
    private Spinner spinner;
    private GridLayout gridMain, gridMain2;
    private ArrayAdapter<String> adapter;
    private AutoCompleteTextView urlText;
    private ListView listView;
    private final int REQUEST_CODE = 1;

    //Initialisation of Java fields.
    private ArrayList<String> urlList;
    public int listCounter = 0;
    public int counter = 0;

    //Initialisation of Firebase fields.
    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabaseUrls;

    /**
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_url);

        listView = (ListView) findViewById(R.id.ListViewUrl);
        urlText = (AutoCompleteTextView) findViewById(R.id.urlText);

        gridMain = (GridLayout) findViewById(R.id.gridMain);
        gridMain2 = (GridLayout) findViewById(R.id.gridMain2);
        setGridMenu(gridMain);
        setGridMenu2(gridMain2);

        spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapterSpinner = ArrayAdapter.createFromResource(this, R.array.urls, R.layout.custom_listview);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapterSpinner);

        urlList = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(UrlActivity.this, R.layout.custom_listview, urlList);
        listView.setAdapter(adapter);
        registerForContextMenu(listView);

        //Prevent keyboard opening on activity launch.
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        //Allows users to pick from premade templates that can be used to help input
        //URLs into the EditText field.
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                urlText.setText(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        mAuth = FirebaseAuth.getInstance();
        mUserDatabaseUrls = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid()).child("URLS");

        //When an item in the list is clicked we remove it from the ListView.
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setToastMessage("Clicked: " + position);
                urlList.remove(position);
                adapter.notifyDataSetChanged();
                listCounter--;
            }
        });

    }

    /**
     * Method for saving the entered URLs of the user
     * Iterates through the arrayList urlList and for each URL pushes it to the Firebase realtime Database.
     * New FireBase Tree Structure:
     *  * Users (root)
     *      -> UID (parent)
     *            -> URL       (value)
     *            -> firstname (value)
     *            -> surname   (value)
     *            -> userID    (value)
     *            -> URLS      (parent)
     *                   -> url1 (value)
     *                   -> url2 (value)
     *
     * We save url's to the Firebase Database.
     * Finally we start the HomeScreenActivity.
     */
    private void saveUserProfile() {
        int i = 1;
        for(String urls: urlList){
            String Url = urls.toString();
            mUserDatabaseUrls.child("url" + i).setValue(Url);
            i++;

        }
        finish();
        Intent moveToHome = new Intent(UrlActivity.this, HomeScreenActivity.class);
        moveToHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(moveToHome);
    }

    /**
     * Method for displaying the cardView.
     * Similar functionality to a Button however visually different (for UX/ consistency across application).
     * Sets onClick for the buttons allowing:
     * The user to return home.
     * Adding the url to the Listview
     * Displaying the tootlip.
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
                        setToastMessage("Back button clicked");
                        Intent intent = new Intent(UrlActivity.this, ProfileSetupActivity.class);
                        startActivity(intent);
                    }
                    if (cardNumber == 1) {
                        urlList.add(urlText.getText().toString());
                        urlText.setText("");
                        adapter.notifyDataSetChanged();
                        listCounter++;
                    }
                    if (cardNumber == 2) {
                        if(counter%2==0) {
                            tooltip = new Tooltip.Builder(gridMain)
                                    .setText("To add a URL, Phone number or any other piece of information, " +
                                            "please either use the drop down menu templates and fill in the " +
                                            "details required, or manually type in the details. " +
                                            "Click the Add Url button to add the information and click on the item in the list to remove it. " +
                                            "Once you have finished click save")
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
     * Method for displaying the cardView.
     * Similar functionality to a Button however visually different (for UX/ consistency across application).
     * Sets onClick for the single button allowing the user to return home
     *
     * @param gridMain the ViewGroup to display the cardView.
     */
    private void setGridMenu2(GridLayout gridMain) {
        for (int i = 0; i < gridMain.getChildCount(); i++) {
            CardView cardView = (CardView) gridMain.getChildAt(i);
            final int cardNumber = i;
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (cardNumber == 0) {
                        setToastMessage("Save button clicked");
                        saveUserProfile();
                        Intent intent = new Intent(UrlActivity.this, HomeScreenActivity.class);
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

