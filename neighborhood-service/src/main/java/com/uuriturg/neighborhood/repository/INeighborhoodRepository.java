package com.uuriturg.neighborhood.repository;

import com.uuriturg.neighborhood.domain.Neighborhood;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface INeighborhoodRepository extends CrudRepository<Neighborhood, UUID> {

    Optional<Neighborhood> findBySlug(String slug);

    Optional<Neighborhood> findByNameIgnoreCase(String name);
}
