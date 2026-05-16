package com.uuriturg.scraper;

import com.uuriturg.scraper.controller.ScraperController;
import com.uuriturg.scraper.dto.ScrapeJobResponse;
import com.uuriturg.scraper.dto.ScrapeStatusResponse;
import com.uuriturg.scraper.scraper.ScraperOrchestrator;
import com.uuriturg.scraper.service.ScrapeJobService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ScraperController.class)
class ScraperControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ScrapeJobService scrapeJobService;

    @MockitoBean
    private ScraperOrchestrator scraperOrchestrator;

    private static final UUID JOB_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");

    // ─── Happy path tests ──────────────────────────────────────────────────────

    @Test
    void triggerScrape_returns202_withJobId() throws Exception {
        ScrapeJobResponse job = ScrapeJobResponse.builder()
                .jobId(JOB_ID)
                .source("KV_EE")
                .status("RUNNING")
                .startedAt(LocalDateTime.now())
                .listingsFound(0)
                .newListings(0)
                .build();

        when(scrapeJobService.startJob(any())).thenReturn(job);
        doNothing().when(scraperOrchestrator).triggerAsync(any());

        mockMvc.perform(post("/scraper/trigger"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.jobId").value(JOB_ID.toString()))
                .andExpect(jsonPath("$.status").value("RUNNING"))
                .andExpect(jsonPath("$.message").value("Scrape job started"));
    }

    @Test
    void getStatus_returns200_withStatusResponse() throws Exception {
        ScrapeStatusResponse status = ScrapeStatusResponse.builder()
                .lastScrapeTime(LocalDateTime.now().minusHours(2))
                .totalActiveListings(42L)
                .currentJobStatus("IDLE")
                .currentJobId(null)
                .build();

        when(scrapeJobService.getStatus()).thenReturn(status);

        mockMvc.perform(get("/scraper/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalActiveListings").value(42))
                .andExpect(jsonPath("$.currentJobStatus").value("IDLE"));
    }

    @Test
    void getRecentJobs_returns200_withJobList() throws Exception {
        ScrapeJobResponse job = ScrapeJobResponse.builder()
                .jobId(JOB_ID)
                .source("KV_EE")
                .status("COMPLETED")
                .startedAt(LocalDateTime.now().minusHours(6))
                .completedAt(LocalDateTime.now().minusHours(5))
                .listingsFound(35)
                .newListings(8)
                .build();

        when(scrapeJobService.getRecentJobs()).thenReturn(List.of(job));

        mockMvc.perform(get("/scraper/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("COMPLETED"))
                .andExpect(jsonPath("$[0].listingsFound").value(35))
                .andExpect(jsonPath("$[0].newListings").value(8));
    }

    // ─── Error case tests ──────────────────────────────────────────────────────

    @Test
    void getStatus_returns500_whenServiceThrows() throws Exception {
        when(scrapeJobService.getStatus())
                .thenThrow(new RuntimeException("Database connection lost"));

        mockMvc.perform(get("/scraper/status"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"));
    }
}
