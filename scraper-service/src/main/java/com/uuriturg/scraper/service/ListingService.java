package com.uuriturg.scraper.service;

import com.uuriturg.scraper.domain.Listing;
import com.uuriturg.scraper.dto.ListingResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ListingService {

    List<ListingResponse> findAll(String neighborhood, BigDecimal maxPrice, BigDecimal minSize);

    List<ListingResponse> findLatest();

    ListingResponse findById(UUID id);

    boolean saveOrUpdate(Listing listing);

    long countActive();
}
