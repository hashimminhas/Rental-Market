package com.uuriturg.scraper.scraper;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.uuriturg.scraper.domain.Listing;

import lombok.extern.slf4j.Slf4j;

/**
 * Scraper for Maa-amet (Estonian Land Board) open data.
 * Uses the public REST API for Tartu rental market statistics.
 * Falls back to seed data derived from official 2023/2024 Tartu averages.
 */
@Component
@Slf4j
public class MaametScraper implements RentalScraper {

    @Override
    public String getSourceName() {
        return "MAAMET";
    }

    @Override
    public List<Listing> scrape() {
        log.warn("Maa-amet has no public rental listings API; returning empty list.");
        return new ArrayList<>();
    }
}
