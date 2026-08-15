package de.knollfrank.extensionsformaps.common;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.List;
import java.util.Optional;

public class ListsTest {

    @Test
    public void testGetInit_withElements() {
        // Given
        final List<String> list = List.of("A", "B", "C");

        // When
        final Optional<List<String>> init = Lists.getInit(list);

        // Then
        assertEquals(Optional.of(List.of("A", "B")), init);
    }

    @Test
    public void testGetInit_withOneElement() {
        // Given
        final List<String> list = List.of("A");

        // When
        final Optional<List<String>> init = Lists.getInit(list);

        // Then
        assertEquals(Optional.of(List.of()), init);
    }

    @Test
    public void testGetInit_empty() {
        // Given
        final List<String> emptyList = List.of();

        // When
        final Optional<List<String>> init = Lists.getInit(emptyList);

        // Then
        assertEquals(Optional.empty(), init);
    }

    @Test
    public void testGetLast_withElements() {
        // Given
        final List<String> list = List.of("A", "B", "C");

        // When
        final Optional<String> last = Lists.getLast(list);

        // Then
        assertEquals(Optional.of("C"), last);
    }

    @Test
    public void testGetLast_empty() {
        // Given
        final List<String> emptyList = List.of();

        // When
        final Optional<String> last = Lists.getLast(emptyList);

        // Then
        assertEquals(Optional.empty(), last);
    }

    @Test
    public void testAsInitAndLast() {
        // Given
        final List<String> list = List.of("A", "B", "C");

        // When
        final Optional<InitAndLast<String>> initAndLast = Lists.asInitAndLast(list);

        // Then
        assertEquals(Optional.of(new InitAndLast<>(List.of("A", "B"), "C")), initAndLast);
    }
}
