package de.KnollFrank.routeoptimizerforgooglemaps.feature;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.KnollFrank.routeoptimizerforgooglemaps.R;

public class ScanAddressActivity extends AppCompatActivity {

    private static final String TAG = "ScanAddressActivity";
    private static final int CAMERA_PERMISSION_CODE = 100;

    public interface ScanResultCallback {
        void onAddressScanned(String address);
    }

    private static ScanResultCallback callback;

    public static void setCallback(ScanResultCallback cb) {
        callback = cb;
    }

    private PreviewView previewView;
    private AddressOverlayView addressOverlay;
    private ExecutorService cameraExecutor;
    private View cardResult;
    private EditText etResult;
    private View progressRefining;
    private ScannedAddressRefiner.RefinedResult currentDetection;
    private boolean isAnalyzing = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_address);

        previewView = findViewById(R.id.previewView);
        addressOverlay = findViewById(R.id.addressOverlay);
        cardResult = findViewById(R.id.cardResult);
        etResult = findViewById(R.id.etResult);
        progressRefining = findViewById(R.id.progressRefining);

        findViewById(R.id.btnRetry).setOnClickListener(v -> {
            cardResult.setVisibility(View.GONE);
            currentDetection = null;
            isAnalyzing = true;
        });
        findViewById(R.id.btnDone).setOnClickListener(v -> {
            if (callback != null) {
                callback.onAddressScanned(etResult.getText().toString());
            }
            finish();
        });

        cameraExecutor = Executors.newSingleThreadExecutor();

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, this::processImage);

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Use case binding failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void processImage(@NonNull ImageProxy imageProxy) {
        @SuppressWarnings("UnsafeOptInUsageError")
        android.media.Image mediaImage = imageProxy.getImage();
        if (mediaImage != null && isAnalyzing) {
            int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
            int width = imageProxy.getWidth();
            int height = imageProxy.getHeight();
            InputImage image = InputImage.fromMediaImage(mediaImage, rotationDegrees);
            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

            recognizer.process(image)
                    .addOnSuccessListener(visionText -> {
                        cameraExecutor.execute(() -> {
                            Optional<ScannedAddressRefiner.RefinedResult> result = ScannedAddressRefiner.refine(this, visionText);
                            runOnUiThread(() -> {
                                if (result.isPresent() && isAnalyzing) {
                                    currentDetection = result.get();
                                    addressOverlay.updateBounds(currentDetection.bounds(), width, height);
                                    
                                    // Auto-detect and show results
                                    isAnalyzing = false;
                                    etResult.setText(currentDetection.address());
                                    cardResult.setVisibility(View.VISIBLE);
                                    addressOverlay.updateBounds(null, 0, 0);
                                } else if (isAnalyzing) {
                                    addressOverlay.updateBounds(null, 0, 0);
                                }
                            });
                        });
                    })
                    .addOnCompleteListener(task -> imageProxy.close());
        } else {
            imageProxy.close();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
        callback = null;
    }
}
