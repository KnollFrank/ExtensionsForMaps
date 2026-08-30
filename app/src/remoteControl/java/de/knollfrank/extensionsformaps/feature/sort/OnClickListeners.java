package de.knollfrank.extensionsformaps.feature.sort;

import android.view.View;

record OnClickListeners(View.OnClickListener sortButtonListener,
                        View.OnClickListener settingsButtonListener) {

    public static OnClickListeners fromSortButtonListenerAndSettingsButtonListener(final View.OnClickListener sortButtonListener,
                                                                                   final View.OnClickListener settingsButtonListener) {
        return new OnClickListeners(sortButtonListener, settingsButtonListener);
    }
}
