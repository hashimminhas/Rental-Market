package com.uuriturg.scraper.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A scraped rental listing from KV.ee or City24")
public class ListingResponse {

    @Schema(description = "Unique listing identifier", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID listingId;

    @Schema(description = "Source portal the listing was scraped from", example = "KV_EE", allowableValues = {"KV_EE", "CITY24", "RENDIN"})
    private String source;

    @Schema(description = "ID of the listing on the source portal", example = "98765")
    private String externalId;

    @Schema(description = "Listing title from the source portal", example = "2-room apartment in Kesklinn")
    private String title;

    @Schema(description = "Monthly rent in EUR", example = "550.00")
    private BigDecimal price;

    @Schema(description = "Apartment size in m²", example = "48.50")
    private BigDecimal size;

    @Schema(description = "Price per m² in EUR, computed automatically", example = "11.34")
    private BigDecimal pricePerSqm;

    @Schema(description = "Number of rooms", example = "2")
    private Integer rooms;

    @Schema(description = "Tartu neighborhood name", example = "Kesklinn")
    private String neighborhood;

    @Schema(description = "Street address", example = "Riia 10")
    private String street;

    @Schema(description = "City", example = "Tartu")
    private String city;

    @Schema(description = "Postal code", example = "51013")
    private String postalCode;

    @Schema(description = "Direct URL to the listing on the source portal", example = "https://www.kv.ee/kinnisvara/98765")
    private String url;

    @Schema(description = "Timestamp when this listing was last scraped")
    private LocalDateTime scrapedAt;

    @Schema(description = "Whether the listing is currently active on the source portal", example = "true")
    private Boolean isActive;

    @Schema(description = "Timestamp when this listing was first added to the database")
    private LocalDateTime createdAt;

    @Schema(description = "True for seed/demo listings, false for real scraped listings", example = "false")
    private Boolean synthetic;
}
