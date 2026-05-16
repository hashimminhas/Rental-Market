package com.uuriturg.analytics.repository;

import com.uuriturg.analytics.domain.NeighborhoodSnapshot;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface INeighborhoodSnapshotRepository extends CrudRepository<NeighborhoodSnapshot, UUID> {

    List<NeighborhoodSnapshot> findByNeighborhoodOrderByDateDesc(String neighborhood);

    Optional<NeighborhoodSnapshot> findFirstByNeighborhoodOrderByDateDesc(String neighborhood);

    List<NeighborhoodSnapshot> findByNeighborhoodAndDateBetweenOrderByDateAsc(
            String neighborhood, LocalDate from, LocalDate to);

    List<NeighborhoodSnapshot> findByDateOrderByAveragePriceAsc(LocalDate date);
}
