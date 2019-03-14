package com.example.harry.socialqrapp;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Class handles all the extra methods the other activities may require.
 */
public class Utility {

    // /Initialisation of Java fields.
    private static boolean dev;

    //Initialisation of Firebase fields.
    private static FirebaseDatabase myDatabase;

    /**
     * Sets offline persistence
     * Must be called before any initialisation of a database reference
     * @return
     */
    public static FirebaseDatabase getDatabase() {
        if (myDatabase == null) {
            myDatabase = FirebaseDatabase.getInstance();
            myDatabase.setPersistenceEnabled(true);
        }
        return myDatabase;
    }

    /**
     * Wraps toast messages in a boolean check preventing the messages
     * being printed. It is used for debugging.
     * @return boolean false
     */
    public static boolean devMode() {
        dev = false;
        return dev;
    }
}