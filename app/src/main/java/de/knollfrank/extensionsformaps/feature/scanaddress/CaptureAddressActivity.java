package de.knollfrank.extensionsformaps.feature.scanaddress;

import static de.knollfrank.extensionsformaps.accessibility.PackageNames.GEMINI_APP_PACKAGE;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;

public class CaptureAddressActivity extends AppCompatActivity {

    private static final String TAG = CaptureAddressActivity.class.getSimpleName();

    private Uri cameraImageUri;
    private Uri finalImageUri;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            launchMediaChooser();
                        } else {
                            Log.e(TAG, "Camera permission denied");
                            finish();
                        }
                    });

    private final ActivityResultLauncher<Intent> mediaLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            final Intent data = result.getData();
                            finalImageUri =
                                    data != null && data.getData() != null ?
                                            // User picked an existing photo from gallery
                                            data.getData() :
                                            // User took a new photo with the camera
                                            cameraImageUri;
                            sendToGemini();
                        } else {
                            finish();
                        }
                    });

    @Override
    protected void onCreate(final @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "CaptureAddressActivity started");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchMediaChooser();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void launchMediaChooser() {
        try {
            final File storageDir = new File(getCacheDir(), "images");
            if (!storageDir.exists() && !storageDir.mkdirs()) {
                Log.e(TAG, "Could not create images directory");
            }
            final File imageFile = new File(storageDir, "capture_" + System.currentTimeMillis() + ".jpg");
            cameraImageUri =
                    FileProvider.getUriForFile(
                            this,
                            getPackageName() + ".fileprovider",
                            imageFile);

            // Intent 1: Take a photo
            final Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
            captureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            // Intent 2: Pick from gallery
            final Intent pickIntent = new Intent(Intent.ACTION_GET_CONTENT);
            pickIntent.addCategory(Intent.CATEGORY_OPENABLE);
            pickIntent.setType("image/*");

            // Chooser: Let the system show all options (Camera, Gallery, etc.)
            final Intent chooserIntent = Intent.createChooser(pickIntent, "Foto aufnehmen oder auswählen");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{captureIntent});

            mediaLauncher.launch(chooserIntent);
        } catch (final Exception e) {
            Log.e(TAG, "Error preparing media chooser", e);
            finish();
        }
    }

    private void sendToGemini() {
        if (finalImageUri == null) {
            finish();
            return;
        }

        final Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/jpeg");
        shareIntent.putExtra(Intent.EXTRA_STREAM, finalImageUri);

        // Put the prompt in BOTH common text fields to increase compatibility
        shareIntent.putExtra(Intent.EXTRA_TEXT, AIPrompt.getAIPrompt());
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Address Extraction");

        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // Target Gemini specifically
        shareIntent.setPackage(GEMINI_APP_PACKAGE);

        try {
            startActivity(shareIntent);
        } catch (final Exception e) {
            Log.w(TAG, "Gemini app not found via package name, trying intent-only");
            shareIntent.setPackage(null);
            startActivity(Intent.createChooser(shareIntent, "Bild an Gemini senden"));
        }
        finish();
    }
}
