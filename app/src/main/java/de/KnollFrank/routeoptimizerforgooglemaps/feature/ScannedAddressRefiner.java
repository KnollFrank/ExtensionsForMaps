package de.KnollFrank.routeoptimizerforgooglemaps.feature;

import android.content.Context;
import android.graphics.Rect;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.google.android.gms.tasks.Tasks;
import com.google.mlkit.nl.entityextraction.Entity;
import com.google.mlkit.nl.entityextraction.EntityAnnotation;
import com.google.mlkit.nl.entityextraction.EntityExtraction;
import com.google.mlkit.nl.entityextraction.EntityExtractor;
import com.google.mlkit.nl.entityextraction.EntityExtractorOptions;
import com.google.mlkit.vision.text.Text;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class ScannedAddressRefiner {

    private static final String TAG = "ScannedAddressRefiner";
    private static EntityExtractor entityExtractor;

    public record RefinedResult(String address, Rect bounds) {}

    public static Optional<RefinedResult> refine(Context context, Text visionText) {
        if (visionText == null || visionText.getText().isBlank()) {
            return Optional.empty();
        }

        // 1. Semantic Extraction: Find the address using ML Kit Entity Extraction
        String addressSnippet = extractAddressEntity(context, visionText.getText());
        if (addressSnippet == null) {
            return Optional.empty();
        }

        // 2. Spatial Mapping: Find which OCR block contains this address
        Rect bounds = findBoundsForText(visionText, addressSnippet);

        // 3. Normalization: Use Geocoder to clean up the address
        final Geocoder geocoder = new Geocoder(context);
        Optional<String> normalized = geocode(geocoder, addressSnippet);

        return normalized.map(s -> new RefinedResult(s, bounds));
    }

    private static String extractAddressEntity(Context context, String fullText) {
        if (entityExtractor == null) {
            entityExtractor = EntityExtraction.getClient(
                    new EntityExtractorOptions.Builder(EntityExtractorOptions.GERMAN).build());
        }

        try {
            // Ensure model is available (blocks current thread, which is fine since we are on background thread)
            Tasks.await(entityExtractor.downloadModelIfNeeded());
            List<EntityAnnotation> annotations = Tasks.await(entityExtractor.annotate(fullText));

            for (EntityAnnotation annotation : annotations) {
                for (Entity entity : annotation.getEntities()) {
                    if (entity.getType() == Entity.TYPE_ADDRESS) {
                        return annotation.getAnnotatedText();
                    }
                }
            }
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Entity extraction failed", e);
        }
        return null;
    }

    private static Rect findBoundsForText(Text visionText, String snippet) {
        // Find the block that most closely matches or contains the snippet
        for (Text.TextBlock block : visionText.getTextBlocks()) {
            if (block.getText().contains(snippet) || snippet.contains(block.getText())) {
                return block.getBoundingBox();
            }
        }
        // Fallback: look at individual lines
        for (Text.TextBlock block : visionText.getTextBlocks()) {
            for (Text.Line line : block.getLines()) {
                if (line.getText().contains(snippet) || snippet.contains(line.getText())) {
                    return line.getBoundingBox();
                }
            }
        }
        return null;
    }

    private static Optional<String> geocode(Geocoder geocoder, String text) {
        try {
            List<Address> addresses = geocoder.getFromLocationName(text, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                if (address.getLocality() != null) {
                    return Optional.of(address.getAddressLine(0));
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Geocoding error: " + e.getMessage());
        }
        return Optional.empty();
    }
}
