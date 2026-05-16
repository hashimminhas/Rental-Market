package com.uuriturg.neighborhood.exception;

public class NeighborhoodNotFoundException extends RuntimeException {

    public NeighborhoodNotFoundException(String slug) {
        super("Neighborhood not found: " + slug);
    }
}
