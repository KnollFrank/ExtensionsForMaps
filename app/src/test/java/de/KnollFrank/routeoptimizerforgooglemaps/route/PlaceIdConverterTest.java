package de.KnollFrank.routeoptimizerforgooglemaps.route;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PlaceIdConverterTest {

    private static final String VALID_OFFICIAL_ID_CHI = "ChIJgdDN7dT6mUcRjacz_s6uCKw";
    private static final String VALID_OFFICIAL_ID_GHI = "GhIJQWDl0CIeQUARxks3icF8U8A";
    private static final String VALID_UNDOCUMENTED_ID = "0x4799fc4b13515dd5:0x345201aaff119b3a";

    @Test
    public void toOfficialPlaceId_and_toUndocumentedPlaceId_areInverse_CHI() {
        toOfficialPlaceId_and_toUndocumentedPlaceId_areInverse(new OfficialPlaceId(VALID_OFFICIAL_ID_CHI));
    }

    @Test
    public void toOfficialPlaceId_and_toUndocumentedPlaceId_areInverse_GHI() {
        toOfficialPlaceId_and_toUndocumentedPlaceId_areInverse(new OfficialPlaceId(VALID_OFFICIAL_ID_GHI));
    }

    @Test
    public void toUndocumentedPlaceId_and_toOfficialPlaceId_areInverse() {
        // Given
        final UndocumentedPlaceId originalUndocumented = new UndocumentedPlaceId(VALID_UNDOCUMENTED_ID);

        // When
        UndocumentedPlaceId reconstructedUndocumented =
                PlaceIdConverter.toUndocumentedPlaceId(
                        PlaceIdConverter.toOfficialPlaceId(originalUndocumented));

        // Then
        assertEquals(
                "Die Invertierung von Undocumented -> Official -> Undocumented ist fehlgeschlagen.",
                originalUndocumented,
                reconstructedUndocumented);
    }

    private static void toOfficialPlaceId_and_toUndocumentedPlaceId_areInverse(final OfficialPlaceId originalOfficial) {
        // When
        final OfficialPlaceId reconstructedOfficial =
                PlaceIdConverter.toOfficialPlaceId(
                        PlaceIdConverter.toUndocumentedPlaceId(originalOfficial));

        // Then
        assertEquals(
                "Die Invertierung von Official -> Undocumented -> Official ist fehlgeschlagen.",
                originalOfficial,
                reconstructedOfficial);
    }
}