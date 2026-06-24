package de.KnollFrank.routeoptimizerforgooglemaps.route;

import java.util.Iterator;
import java.util.List;

class TokenIterator implements Iterator<String> {

    private final List<String> tokens;
    private int index = 0;

    public TokenIterator(final List<String> tokens) {
        this.tokens = tokens;
    }

    @Override
    public boolean hasNext() {
        return index < tokens.size();
    }

    @Override
    public String next() {
        return tokens.get(index++);
    }
}