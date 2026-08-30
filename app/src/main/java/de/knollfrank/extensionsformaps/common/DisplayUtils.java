package de.knollfrank.extensionsformaps.common;

import android.util.DisplayMetrics;
import android.util.TypedValue;

public class DisplayUtils {

    private DisplayUtils() {
    }

    public static int dipToPx(final int dip, final DisplayMetrics displayMetrics) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, displayMetrics);
    }
}