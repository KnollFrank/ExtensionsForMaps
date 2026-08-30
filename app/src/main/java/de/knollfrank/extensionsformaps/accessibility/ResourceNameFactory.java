package de.knollfrank.extensionsformaps.accessibility;

import static de.knollfrank.extensionsformaps.accessibility.PackageNames.GOOGLE_MAPS_PACKAGE;

public class ResourceNameFactory {

    public static ResourceName createGoogleMapsResourceName(final String name) {
        return new ResourceName(GOOGLE_MAPS_PACKAGE, name);
    }
}
