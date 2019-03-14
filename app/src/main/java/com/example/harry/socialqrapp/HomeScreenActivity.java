package com.example.harry.socialqrapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Class is for displaying and handling all interactions with users home screen.
 */
public class HomeScreenActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    //Initialisation of widgets.
    private GridLayout gridMain;
    private TextView mValueView;
    private Button btnOpen;
    private CircleImageView mImage;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

    //Initialisation of Java fields.
    private int counter = 0;
    private String url;

    //Initialisation of Firebase fields.
    private FirebaseAuth mAuth;

    /**
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        // Creating objects:
        mAuth = FirebaseAuth.getInstance();
        mImage = (CircleImageView) findViewById(R.id.userImage);
        mValueView = (TextView) findViewById(R.id.valueView);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        btnOpen = (Button) findViewById(R.id.menuBtn);
        gridMain = (GridLayout) findViewById(R.id.gridMain);
        setGridMenu(gridMain);

        // mToggle synchronizes the state of the drawer indicator with the linked mDrawerlayout
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        mToggle.syncState();

        // Creation of our navigationView: this is to be used for our drawerLayout Nav Menu
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        Bitmap bitmap = BitmapFactory.decodeFile("myDir/");

        // Database reference to current user instance and retrieving user details.
        // 'Users' is the table of current users
        // We get current user by using mAuth.getCurrentUser().getUid()
        // And retrieve the child in the realtime database to retrieve users firstName.
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("Users");
        DatabaseReference mRef1 = mRef.child(mAuth.getCurrentUser().getUid());
        DatabaseReference mRef2 = mRef1.child("firstname");
        DatabaseReference mRef3 = mRef1.child("URL");

        mRef3.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null){
                    url = dataSnapshot.getValue().toString();
                    loadImage(url);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // Creates eventListener to retrieve information from real-time database
        // Currently retrieves and displays firstName to textView
        mRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    String value = dataSnapshot.getValue().toString();
                    mValueView.setText("Hi " + value);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // Create button for opening and closing the DrawerMenu on click.
        // If counter == odd open the menu, else if even, we close it.
        btnOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (counter % 2 == 0) {
                    mDrawerLayout.openDrawer(Gravity.RIGHT);
                    counter++;
                } else if (counter % 2 == 1) {
                    mDrawerLayout.closeDrawer(Gravity.RIGHT);
                    counter++;
                }
            }
        });
    }

    /**
     * Method for clicking item in the cardView Menu (Primary page navigation/menu).
     * implements functionality for each item in menu.
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
                        if(Utility.devMode()==true) {
                            setToastMessage("Change URLs button clicked");
                        }
                        Intent intent = new Intent(HomeScreenActivity.this, UpdateUrlsActivity.class);
                        startActivity(intent);
                    }
                    if (cardNumber == 1) {
                        if(Utility.devMode()==true) {
                            setToastMessage("Generate Code button clicked");
                        }
                        Intent intent = new Intent(HomeScreenActivity.this, GenerateQRActivity.class);
                        startActivity(intent);
                    }
                    if (cardNumber == 2) {
                        if(Utility.devMode()==true) {
                            setToastMessage("Friends list button clicked");
                        }
                        Intent intent = new Intent(HomeScreenActivity.this, ScannedCodesActivity.class);
                        startActivity(intent);
                    }
                    if (cardNumber == 3) {
                        if(Utility.devMode()==true) {
                            setToastMessage("Scan QR Code button clicked");
                        }
                        Intent intent = new Intent(HomeScreenActivity.this, ScanCodeActivity.class);
                        startActivity(intent);
                    }
                }
            });
        }
    }

    /**
     * Method for clicking each item in DrawerMenu (Secondary page navigation/menu).
     * Each item has its own implemented functionality.
     *
     * @param item the item clicked.
     * @return
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        // Starts activity to change user settings.
        if (id == R.id.nav_settings) {
            Intent intentSettings = new Intent(HomeScreenActivity.this, ChangeUserInfoActivity.class);
            startActivity(intentSettings);
        }

        // Starts activity to Logout User.
        // Opens Dialogue to check if user wants to logout before proceeding.
        if (id == R.id.nav_logout) {
            AlertDialog.Builder builder = new AlertDialog.Builder(HomeScreenActivity.this);
            builder.setMessage("Are you sure you wish to logout?");
            builder.setCancelable(true);
            builder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    logoutProfile();
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

        // Starts activity that provides information regarding application.
        if (id == R.id.nav_about) {
            Intent intentAbout = new Intent(HomeScreenActivity.this, AboutActivity.class);
            startActivity(intentAbout);
        }
        return false;
    }

    /**
     * Method for checking if item in drawerMenu has been selected.
     *
     * @return selected Item in menu.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Method that when called will logout the current user and returns to MainActivity activity.
     */
    private void logoutProfile() {
        mAuth.signOut();
        Intent intent = new Intent(HomeScreenActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void loadImage(String url){
        Picasso.with(this).load(url).placeholder(R.mipmap.ic_launcher_round)
                .error(R.mipmap.ic_launcher_round)
                .into(mImage, new com.squareup.picasso.Callback(){

                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {

                    }
                });
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
