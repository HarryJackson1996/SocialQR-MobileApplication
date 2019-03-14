package com.example.harry.socialqrapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.Toast;
import com.google.zxing.Result;
import com.journeyapps.barcodescanner.CaptureActivity;

import java.util.Scanner;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.Manifest.permission.CAMERA;

/**
 * Scan QR code Activity for scanning QR codes generated from the GenerateQRActivity
 * however will scan any QR code if need be.
 * Handles the scanning of a QR code.
 */
public class ScanCodeActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler{

    //Initialisation of QR scannerView and camera permission requests
    private static final int REQUEST_CAMERA = 1;
    private ZXingScannerView scannerView;

    /**
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_code);

        // Create Objects
        scannerView = new ZXingScannerView(this);
        setContentView(scannerView);

        // If permission has been granted (true) display toast message else call requestPermission() method.
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if(checkPermission()){
                setToastMessage("Permission Granted");
            }
            else
            {
                requestPermission();
            }
        }
    }

    /**
     * Method for checking if permission for accessing the camera has been granted.
     *
     * @return
     */
    private boolean checkPermission(){
        return (ContextCompat.checkSelfPermission(ScanCodeActivity.this, CAMERA) == PackageManager.PERMISSION_GRANTED);
    }

    /**
     * Method for requesting camera permissions to scan the QR code.
     */
    private void requestPermission(){
        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, REQUEST_CAMERA);
    }

    /**
     * Method handles the permissions associated with the camera.
     * Whether the permissions are accepted or denied.
     *
     * @param requestCode
     * @param permission
     * @param grantResults
     */
    public void onRequestPermissionsResult(int requestCode, String permission[], int grantResults[])
    {
        switch(requestCode)
        {
            case REQUEST_CAMERA:
                if(grantResults.length > 0)
                {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted){
                        setToastMessage("Permission Granted");
                    }
                    else{
                        setToastMessage("Permission Denied");
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                            if(shouldShowRequestPermissionRationale(CAMERA)){
                                displayAlertMessage("You need to allow access for both permissions", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            requestPermissions(new String[]{CAMERA}, REQUEST_CAMERA);
                                        }
                                    }
                                });
                                return;
                            }
                        }
                    }
                }
                break;
        }
    }

    /**
     * Method for displaying the scannerView (QR code scanner).
     */
    @Override
    public void onResume(){
        super.onResume();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checkPermission()){
                if(scannerView == null) {
                    scannerView = new ZXingScannerView(this);
                    setContentView(scannerView);
                }
                scannerView.setResultHandler(this);
                scannerView.startCamera();
            }
            else{
                requestPermission();
            }
        }
    }

    /**
     * Method for stopping the Camera/scannerView.
     */
    @Override
    public void onDestroy(){
        super.onDestroy();
        scannerView.stopCamera();
    }

    /**
     * Method that displays Alert Message Dialogue
     *
     * @param message
     * @param listener
     */
    public void displayAlertMessage(String message, DialogInterface.OnClickListener listener)
    {
        new AlertDialog.Builder(ScanCodeActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", listener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    /**
     *  Method to handle the scanned QR code
     *  Gets the result from the QR to Text format.
     *  Uses the intent.putExtra to send the result to another activity,
     *  this activity being ScanResultActivity.
     *
     * @param result
     */
    @Override
    public void handleResult(Result result) {
        final String scanResult = result.getText();
        Intent intent = new Intent(ScanCodeActivity.this, ScanResultActivity.class);
        intent.putExtra("infoQR", scanResult);
        startActivity(intent);
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
