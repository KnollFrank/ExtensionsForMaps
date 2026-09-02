package de.knollfrank.extensionsformaps;

import static de.knollfrank.extensionsformaps.accessibility.PackageNames.GOOGLE_APP_PACKAGE;
import static de.knollfrank.extensionsformaps.accessibility.PackageNames.GOOGLE_MAPS_PACKAGE;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class ResourceFinderTest {

    @Test
    public void findGoogleMapsResources() {
        final ResourceFinder resourceFinder =
                new ResourceFinder(
                        GOOGLE_MAPS_PACKAGE,
                        new ResourceFinder.Candidates(
                                List.of("stop", "waypoint", "add"),
                                List.of("stopp", "halt", "zwischen", "add stop", "share", "teilen")));
        resourceFinder.findAndLogResources();
    }

    @Test
    public void findGoogleAppResources() {
        final ResourceFinder resourceFinder =
                new ResourceFinder(
                        GOOGLE_APP_PACKAGE,
                        new ResourceFinder.Candidates(
                                List.of(),
                                List.of("Ask anything", "Send", "AI Mode", "Take a photo")));
        resourceFinder.findAndLogResources();
    }
}
