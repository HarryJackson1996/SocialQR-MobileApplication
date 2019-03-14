package com.example.harry.socialqrapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.GridLayout;
import android.widget.Toast;

/**
 * This Class is for displaying information regarding the application.
 * Currently placeholder as the application will not be released.
 */
public class AboutActivity extends AppCompatActivity {

    //Initialisation of widgets.
    private GridLayout gridMain;

    /**
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        gridMain = (GridLayout) findViewById(R.id.gridMain);
        setGridMenu(gridMain);
    }

    /**
     * Method for clicking item in the cardView Menu (Primary page navigation/menu).
     * Implements functionality for each item in menu.
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
                        setToastMessage("Return home button clicked");
                        Intent intent = new Intent(AboutActivity.this, HomeScreenActivity.class);
                        startActivity(intent);
                    }
                }
            });
        }
    }

    /**
     * Method for creating Toast messages
     *
     * @param message to be displayed
     */
    private void setToastMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}
