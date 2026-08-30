package de.knollfrank.extensionsformaps.feature.sort;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.view.WindowManager;

import de.knollfrank.extensionsformaps.accessibility.RouteUrlRequester;

public class SortFeatureFactory {

    public static SortFeature createSortFeature(final RouteUrlRequester routeUrlRequester,
                                                final RouteUrlRequester.RouteUrlCallback onRouteUrlExtracted,
                                                final AccessibilityService accessibilityService) {
        return new SortFeature(
                new Buttons(
                        (WindowManager) accessibilityService.getSystemService(Context.WINDOW_SERVICE),
                        accessibilityService,
                        Buttons.OnClickListeners.fromSortButtonListenerAndSettingsButtonListener(
                                view -> routeUrlRequester.requestRouteUrl(onRouteUrlExtracted),
                                view -> new SettingsDialog(accessibilityService).show())));

    }
}
