package de.knollfrank.extensionsformaps.feature;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;

public class CaptureAddressActivity extends AppCompatActivity {

    private static final String TAG = "CaptureAddressActivity";
    private static final String GEMINI_PKG = "com.google.android.apps.bard";
    
    private Uri imageUri;

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    launchCamera();
                } else {
                    Log.e(TAG, "Camera permission denied");
                    finish();
                }
            }
    );

    private final ActivityResultLauncher<Uri> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            success -> {
                if (success) {
                    sendToGemini();
                } else {
                    Log.e(TAG, "Image capture failed or cancelled");
                    finish();
                }
            }
    );

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "CaptureAddressActivity started");

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void launchCamera() {
        try {
            File storageDir = new File(getCacheDir(), "images");
            if (!storageDir.exists() && !storageDir.mkdirs()) {
                Log.e(TAG, "Could not create images directory");
            }
            File imageFile = new File(storageDir, "capture_" + System.currentTimeMillis() + ".jpg");
            imageUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", imageFile);
            
            Log.d(TAG, "Launching camera with URI: " + imageUri);
            takePictureLauncher.launch(imageUri);
        } catch (Exception e) {
            Log.e(TAG, "Error preparing camera launch", e);
            finish();
        }
    }

    private void sendToGemini() {
        // We use ACTION_SEND with both image and text.
        // Some apps (like Gemini/Assistant) handle this combination specifically.
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/jpeg");
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        
        // Put the prompt in BOTH common text fields to increase compatibility
        shareIntent.putExtra(Intent.EXTRA_TEXT, ScanAddressFeature.AI_PROMPT);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Address Extraction");
        
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        
        // Target Gemini specifically
        shareIntent.setPackage(GEMINI_PKG);

        try {
            startActivity(shareIntent);
        } catch (Exception e) {
            Log.w(TAG, "Gemini app not found via package name, trying intent-only");
            shareIntent.setPackage(null);
            
            // If direct package failed, we try to find an activity that can handle it
            // or show the chooser as a fallback.
            startActivity(Intent.createChooser(shareIntent, "Bild an Gemini senden"));
        }
        finish();
    }
}
