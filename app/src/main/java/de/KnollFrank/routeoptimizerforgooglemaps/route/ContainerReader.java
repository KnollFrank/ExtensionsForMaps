package de.KnollFrank.routeoptimizerforgooglemaps.route;

import com.google.common.collect.ImmutableList;

import java.util.Iterator;
import java.util.List;

// FK-TODO: remove class
class ContainerReader {

    private static final String containerMarker = MarkerFactory.createMarker(1, Datatype.CONTAINER);

    public static boolean isContainer(final String token) {
        return token.startsWith(containerMarker);
    }

    public static List<String> readTokensInContainer(final String token, final Iterator<String> tokenIterator) {
        return getNextTokens(getNumTokensInContainer(token), tokenIterator);
    }

    private static int getNumTokensInContainer(final String token) {
        return Character.getNumericValue(token.charAt(containerMarker.length()));
    }

    private static List<String> getNextTokens(final int numTokens, final Iterator<String> tokenIterator) {
        final ImmutableList.Builder<String> nextTokensBuilder = ImmutableList.builder();
        for (int i = 0; i < numTokens; i++) {
            nextTokensBuilder.add(tokenIterator.next());
        }
        return nextTokensBuilder.build();
    }
}
