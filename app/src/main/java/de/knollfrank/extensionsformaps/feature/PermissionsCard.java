package de.knollfrank.extensionsformaps.feature;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.accessibility.AccessibilityManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

import de.knollfrank.extensionsformaps.BuildConfig;
import de.knollfrank.extensionsformaps.R;
import de.knollfrank.extensionsformaps.databinding.ViewPermissionsBinding;

public class PermissionsCard extends MaterialCardView {

    private ViewPermissionsBinding binding;

    public PermissionsCard(@NonNull final Context context) {
        this(context, null);
    }

    public PermissionsCard(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        this(context, attrs, com.google.android.material.R.attr.materialCardViewElevatedStyle);
    }

    public PermissionsCard(@NonNull final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        binding = ViewPermissionsBinding.inflate(LayoutInflater.from(context), this);
        configurePermissionButtons();
    }

    public void onResume() {
        updatePermissionButtonStates();
    }

    private void configurePermissionButtons() {
        if (!BuildConfig.SHOW_ACCESSIBILITY_SETTINGS) {
            setVisibility(View.GONE);
            return;
        }
        binding.btnPermitAccessibility.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(final View view) {
                        getContext().startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                    }
                });
    }

    private void updatePermissionButtonStates() {
        if (BuildConfig.SHOW_ACCESSIBILITY_SETTINGS) {
            updatePermitAccessibilityButtonState();
        }
    }

    private void updatePermitAccessibilityButtonState() {
        final boolean accessibilityServiceEnabled = isAccessibilityServiceEnabled();
        binding.btnPermitAccessibility.setText(accessibilityServiceEnabled ? R.string.permit_accessibility_done : R.string.permit_accessibility);
    }

    private boolean isAccessibilityServiceEnabled() {
        return isPackageNameEnabled(
                getAccessibilityManager().getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC),
                getContext().getPackageName());
    }

    private AccessibilityManager getAccessibilityManager() {
        return (AccessibilityManager) getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
    }

    private static boolean isPackageNameEnabled(final List<AccessibilityServiceInfo> accessibilityServiceInfos,
                                                final String packageName) {
        return accessibilityServiceInfos
                .stream()
                .map(accessibilityServiceInfo -> accessibilityServiceInfo.getResolveInfo().serviceInfo.packageName)
                .anyMatch(packageName::equals);
    }
}
