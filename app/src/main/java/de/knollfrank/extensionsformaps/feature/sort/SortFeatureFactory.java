package de.knollfrank.extensionsformaps.feature.sort;

import android.accessibilityservice.AccessibilityService;

import de.knollfrank.extensionsformaps.accessibility.RouteUrlRequester;
import de.knollfrank.extensionsformaps.accessibility.wrapper.AccessibilityServiceWrapper;

public class SortFeatureFactory {

    public static SortFeature createSortFeature(final RouteUrlRequester routeUrlRequester,
                                                final RouteUrlRequester.RouteUrlCallback onRouteUrlExtracted,
                                                final AccessibilityService accessibilityService) {
        return new SortFeature(
                new Buttons(
                        new AccessibilityServiceWrapper(accessibilityService).getWindowManager(),
                        accessibilityService,
                        OnClickListeners.fromSortButtonListenerAndSettingsButtonListener(
                                view -> routeUrlRequester.requestRouteUrl(onRouteUrlExtracted),
                                view -> new SettingsDialog(accessibilityService).show())));

    }
}
