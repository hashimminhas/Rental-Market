package com.uuriturg.scraper.scraper;

import com.uuriturg.scraper.domain.Listing;

import java.util.List;

public interface RentalScraper {

    List<Listing> scrape();

    String getSourceName();
}
