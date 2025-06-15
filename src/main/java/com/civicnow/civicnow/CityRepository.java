package com.civicnow.civicnow;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.lang.NonNull;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {
    @NonNull List<City> findAll();

    List<City> findByNameContainingIgnoreCase(String query);
}