package com.example.harry.socialqrapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

/**
 * Main Activity for the initial startup of the Application.
 * Handles the sign-up functionality for the user, and login functionality.
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //Initialisation of widgets.
    private EditText EditTextEmail, EditTextPassword;

    //Initialisation of Firebase fields.
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    /**
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Call in method from external class allowing for offline capabilities.
        Utility.getDatabase();

        EditTextEmail = (EditText) findViewById(R.id.EditTextEmail);
        EditTextPassword = (EditText) findViewById(R.id.EditTextPassword);

        findViewById(R.id.btnSignUp).setOnClickListener(this);
        findViewById(R.id.btnLogin).setOnClickListener(this);

        //Prevent keyboard opening on activity launch.
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                // We check if user exists in FireBase and move to HomeScreen if true.
                // Prevents the user repeatedly signing in.
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    Intent moveToHome = new Intent(MainActivity.this, HomeScreenActivity.class);
                    moveToHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(moveToHome);
                    finish();
                }
            }
        };

        mAuth.addAuthStateListener(mAuthListener);
    }

    /**
     * Method for handling the Login functionality for the User.
     * Checks:
     * If email TextField is empty.
     * If input email matches the a standard email address Pattern.
     * If password TextField is empty.
     * If password input length is less than 5.
     * If both email and password match.
     * We sign into the application which takes us to the HomeScreenActivity.
     */
    private void userLogin() {

        String email = EditTextEmail.getText().toString().trim();
        String password = EditTextPassword.getText().toString().trim();

        if (email.isEmpty()) {
            EditTextEmail.setError("Email is required");
            EditTextEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            EditTextEmail.setError("Please Enter a valid Email");
            EditTextEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            EditTextEmail.setError("Password is required");
            EditTextEmail.requestFocus();
            return;
        }
        if (password.length() < 5) {
            EditTextPassword.setError("Minimum length of password should be 6");
            EditTextPassword.requestFocus();
            return;
        }
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(MainActivity.this, HomeScreenActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    } else {
                        setToastMessage(task.getException().getMessage());
                    }
                }
            });
        }


    }

    /**
     * Method for handling the Sign-up functionality for the User.
     * Checks:
     * If email TextField is empty.
     * If input email matches the a standard email address Pattern.
     * If password TextField is empty.
     * If password input length is less than 5.
     * If Email is not already registered.
     * If both email and password inputs are suitable, we move to the ProfileSetupActivity.class.
     */
    private void userRegister() {
        String email = EditTextEmail.getText().toString().trim();
        String password = EditTextPassword.getText().toString().trim();

        if (email.isEmpty()) {
            EditTextEmail.setError("Email is required");
            EditTextEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            EditTextEmail.setError("Please Enter a valid Email");
            EditTextEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            EditTextEmail.setError("Password is required");
            EditTextEmail.requestFocus();
            return;
        }
        if (password.length() < 5) {
            EditTextPassword.setError("Minimum length of password should be 6");
            EditTextPassword.requestFocus();
            return;
        }
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    startActivity(new Intent(MainActivity.this, ProfileSetupActivity.class));
                } else {
                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                        setToastMessage("You are already registered");
                    } else {
                        setToastMessage(task.getException().getMessage());
                    }
                }
            }
        });

    }

    /**
     * Method for handling the onClick functionality each of the Sign-up and Login Buttons.
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnSignUp:
                userRegister();
                break;
            case R.id.btnLogin:
                userLogin();
                break;
        }
    }

    /**
     * Method for adding the AuthStateListener
     */
    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    /**
     * Method for removing the AuthStateListener
     */
    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthListener);
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





