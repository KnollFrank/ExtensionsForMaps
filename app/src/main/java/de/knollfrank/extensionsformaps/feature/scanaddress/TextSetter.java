package de.knollfrank.extensionsformaps.feature.scanaddress;

import android.accessibilityservice.AccessibilityService;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

class TextSetter {

    private static final String TAG = TextSetter.class.getSimpleName();

    private final AccessibilityService accessibilityService;

    public TextSetter(final AccessibilityService accessibilityService) {
        this.accessibilityService = accessibilityService;
    }

    public boolean setInputText(final AccessibilityNodeInfo node, final String text) {
        return performSetText(node, text) || performCopyPaste(node, text);
    }

    public static boolean performSetText(final AccessibilityNodeInfo node, final String text) {
        return node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, getBundleForSettingText(text));
    }

    private boolean performCopyPaste(final AccessibilityNodeInfo node, final String text) {
        try {
            final ClipboardManager clipboardManager = (ClipboardManager) accessibilityService.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboardManager != null) {
                clipboardManager.setPrimaryClip(ClipData.newPlainText("text", text));
                node.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
                return node.performAction(AccessibilityNodeInfo.ACTION_PASTE);
            }
        } catch (final Exception e) {
            Log.e(TAG, "Error performing copy/paste", e);
        }
        return false;
    }

    private static Bundle getBundleForSettingText(final String text) {
        final Bundle bundle = new Bundle();
        bundle.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
        return bundle;
    }
}
