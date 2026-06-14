package com.example.routeoptimizerforgooglemaps;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.ArrayList;

public class FloatingWidgetService extends Service {

    private static final String CHANNEL_ID = "RouteOptimizerServiceChannel";
    private static final int NOTIFICATION_ID = 1;

    private WindowManager windowManager;
    private View floatingView;
    private ArrayList<String> optimizedStops;
    private int currentStopIndex = 0;

    @Nullable
    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        if (intent != null && intent.hasExtra("OPTIMIZED_STOPS")) {
            optimizedStops = intent.getStringArrayListExtra("OPTIMIZED_STOPS");
            currentStopIndex = 0;
            createNotificationChannel();
            final Notification notification = createNotification();
            startForeground(NOTIFICATION_ID, notification);
            
            if (floatingView == null) {
                setupFloatingWidget();
            }
            
            // Automatically launch the first stop if the list is not empty
            if (optimizedStops != null && !optimizedStops.isEmpty()) {
                launchNextStopNavigation();
            }
        }
        return START_NOT_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Route Optimizer Active",
                    NotificationManager.IMPORTANCE_LOW
            );
            final NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    private Notification createNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Tour Active")
                .setContentText("Navigation overlay is active.")
                .setSmallIcon(android.R.drawable.ic_dialog_map)
                .build();
    }

    private void setupFloatingWidget() {
        floatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_widget, null);

        final int layoutFlag;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutFlag = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutFlag = WindowManager.LayoutParams.TYPE_PHONE;
        }

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 200;

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        windowManager.addView(floatingView, params);

        final View dragHandle = floatingView.findViewById(R.id.ivWidgetDragHandle);
        dragHandle.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(final View v, final MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(floatingView, params);
                        return true;
                }
                return false;
            }
        });

        final Button btnNextStop = floatingView.findViewById(R.id.btnNextStop);
        btnNextStop.setOnClickListener(v -> launchNextStopNavigation());
    }

    private void launchNextStopNavigation() {
        if (optimizedStops == null || currentStopIndex >= optimizedStops.size()) {
            finishTour();
            return;
        }

        final String nextAddress = optimizedStops.get(currentStopIndex);
        currentStopIndex++;

        final Uri gmmIntentUri = Uri.parse("google.navigation:q=" + Uri.encode(nextAddress));
        final Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        mapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            startActivity(mapIntent);
        } catch (final Exception e) {
            Toast.makeText(this, "Google Maps is not installed.", Toast.LENGTH_SHORT).show();
        }

        if (currentStopIndex >= optimizedStops.size()) {
            final Button btnNextStop = floatingView.findViewById(R.id.btnNextStop);
            btnNextStop.setText("BEENDEN");
        }
    }

    private void finishTour() {
        Toast.makeText(this, "Tour finished!", Toast.LENGTH_LONG).show();
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatingView != null && windowManager != null) {
            windowManager.removeView(floatingView);
            floatingView = null;
        }
    }
}
