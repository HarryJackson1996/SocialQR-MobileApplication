package com.example.harry.socialqrapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * This Class is for updating the current users personal information.
 */
public class ChangeUserInfoActivity extends AppCompatActivity {

    //Initialisation of widgets.
    private EditText FirstNameText, SecondNameText;
    private CircleImageView mUserImage;
    private GridLayout gridMain, gridMain2;

    //Initialisation of Firebase fields.
    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;
    private StorageReference mStorageRef;

    //Initialisation of image fields (for retrieving an image from the gallery).
    private Uri file;
    private Uri downloadUri;
    private final int REQUEST_CODE = 1;

    /**
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_user_info);

        FirstNameText = (EditText) findViewById(R.id.FirstNameText);
        SecondNameText = (EditText) findViewById(R.id.SecondNameText);

        mUserImage = (CircleImageView) findViewById(R.id.userImage);

        gridMain = (GridLayout) findViewById(R.id.gridMain);
        gridMain2 = (GridLayout) findViewById(R.id.gridMain3);
        setGridMenu(gridMain);
        setGridMenu2(gridMain2);

        mAuth = FirebaseAuth.getInstance();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://socialqr-ae672.appspot.com");

        mUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CODE);
            }
        });
    }

    /**
     * Method for accessing the current users image gallery and setting the ImageView to the chosen image.
     * Surrounded with try/catch in case image is unsuccessfully uploaded.
     *
     * @param requestCode to identify which intent we came from.
     * @param resultCode  to handle the result of the Activity
     * @param data        the data returned
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
     * Method for displaying the cardView.
     * Similar functionality to a Button however visually different (for UX/ consistency across application).
     * returns user to homescreen Activity.
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
                        Intent moveToHome = new Intent(ChangeUserInfoActivity.this, HomeScreenActivity.class);
                        moveToHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(moveToHome);
                    }
                }
            });
        }

    }

    /**
     * Method for displaying the cardView (updateUserDetails).
     * Similar functionality to a Button however visually different (for UX/ consistency across application).
     * Calls updateUserProfile() method to update all current user information.
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
                        updateUserProfile();
                    }
                }
            });
        }

    }

    /**
     * Method for overriding all user information and images to the Firebase realtime Database and Storage.
     * We access the current users data tree in the database.
     * We update the text fields and image in the realtime database/ storage.
     * Finally we return to the home screen activity.
     */
    private void updateUserProfile() {
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
                    downloadUri = taskSnapshot.getDownloadUrl();
                    mUserDatabase.child("URL").setValue(downloadUri.toString());
                }
            });

            mUserDatabase.child("firstname").setValue(firstname);
            mUserDatabase.child("surname").setValue(surname);
            mUserDatabase.child("userID").setValue(mAuth.getCurrentUser().getUid());
            finish();
            Intent moveToHome = new Intent(ChangeUserInfoActivity.this, HomeScreenActivity.class);
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