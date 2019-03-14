package com.example.harry.socialqrapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * QR check Activity displays the generated QR code
 */
public class QRCheckActivity extends AppCompatActivity {

    //Initialisation of widgets
    private ImageView QRimageView;
    private GridLayout gridMain;

    /**
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcheck);

        // Creating objects.
        QRimageView = (ImageView) findViewById(R.id.imageViewQR);
        gridMain = (GridLayout) findViewById(R.id.gridMain);
        setGridMenu(gridMain);

        // Handles to displaying of the QR code from the previous Activity (GenerateQRActivity)
        if(getIntent().hasExtra("byteArray")) {
            ImageView previewThumbnail = new ImageView(this);
            Bitmap b = BitmapFactory.decodeByteArray(getIntent().getByteArrayExtra("byteArray"),0,getIntent().getByteArrayExtra("byteArray").length);
            QRimageView.setImageBitmap(b);
        }
    }

    /**
     * Method for displaying the cardView.
     * Similar functionality to a Button however visually different (for UX/ consistency across application).
     * Buttons for either returning home or generating another QR code.
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
                    if(cardNumber == 0){
                        if(Utility.devMode()==true) {
                            setToastMessage("Clicked Generate QR code button");
                        }
                        Intent intent = new Intent(QRCheckActivity.this, GenerateQRActivity.class);
                        startActivity(intent);
                    }
                    if (cardNumber == 1) {
                        if(Utility.devMode()==true) {
                            setToastMessage("Return home button clicked");
                        }
                        Intent intent = new Intent(QRCheckActivity.this, HomeScreenActivity.class);
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
