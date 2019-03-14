package com.example.harry.socialqrapp;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Profile Setup Activity for creating the current users profile.
 */
public class ProfileSetupActivity extends AppCompatActivity {

    //Initialisation of widgets.
    private EditText FirstNameText, SecondNameText;
    private CircleImageView mUserImage;
    private GridLayout gridMain2;

    //Initialisation of Firebase fields.
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mUserDatabase;
    private StorageReference mStorageRef;

    //Initialisation of image fields (for retrieving an image from the gallery).
    private Uri file;
    private final int REQUEST_CODE = 1;

    /**
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        // Creating objects:
        gridMain2 = (GridLayout) findViewById(R.id.gridMain2);
        setGridMenu(gridMain2);
        mUserImage = (CircleImageView) findViewById(R.id.userImage);
        FirstNameText = (EditText) findViewById(R.id.FirstNameText);
        SecondNameText = (EditText) findViewById(R.id.SecondNameText);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                //We check if user exists in FireBase and move to HomeScreen if true.
                //Prevents the user repeatedly signing in.
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    finish();
                    Intent moveToHome = new Intent(ProfileSetupActivity.this, HomeScreenActivity.class);
                    moveToHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(moveToHome);
                }
            }
        };

        mUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CODE);
            }
        });

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://socialqr-ae672.appspot.com");

    }

    /**
     * Method for accessing the current users image gallery and setting the ImageView to the chosen image.
     * Surrounded with try/catch incase image is unsuccessfully uploaded.
     *
     * @param requestCode to identify which intent we came from.
     * @param resultCode to handle the result of the Activity
     * @param data the data returned
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            file = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), file);
                mUserImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * Method for displaying the cardView (Save Details).
     * Similar functionality to a Button however visually different (for UX/ consistency across application).
     * Calls saveUserProfile() methoo
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
                        saveUserProfile();
                    }
                }
            });
        }

    }

    /**
     * Method for saving all user information and images to the Firebase realtime Database and Storage.
     * We create a new user profile in the Database under the root (Users).
     * The parent Node is set to the UserID (UID) as its uniquely identifiable.
     * This contains the values associated with the current user (from the TextFields):
     * the URL (reference to Firebase Storage location), firstname, surname and UserID.
     * FireBase Tree Structure:
     * Users (root)
     *      -> UID (parent)
     *            -> URL       (value)
     *            -> firstname (value)
     *            -> surname   (value)
     *            -> userID    (value)
     *
     * We save the current Image to the Firebase Database.
     * Finally we start the UrlActivity.
     */
    private void saveUserProfile() {
        final String firstname, surname;

        firstname = FirstNameText.getText().toString().trim();
        surname = SecondNameText.getText().toString().trim();

        final StorageReference ref = mStorageRef.child("images/" + UUID.randomUUID().toString());

        if (TextUtils.isEmpty(firstname)) {
            setToastMessage("Please Enter Name");
        } else if (TextUtils.isEmpty(surname)) {
            setToastMessage("Please Enter Surname");
        } else if (file == null) {
            setToastMessage("Please Select Picture");
        } else {
            ref.putFile(file).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUri = taskSnapshot.getDownloadUrl();
                    mUserDatabase.child("URL").setValue(downloadUri.toString());

                }
            });

            mUserDatabase.child("firstname").setValue(firstname);
            mUserDatabase.child("surname").setValue(surname);
            mUserDatabase.child("userID").setValue(mAuth.getCurrentUser().getUid());
            finish();
            Intent moveToHome = new Intent(ProfileSetupActivity.this, UrlActivity.class);
            moveToHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(moveToHome);
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
