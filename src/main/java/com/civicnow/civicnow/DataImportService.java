package com.civicnow.civicnow;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStreamReader;

@Service
@RequiredArgsConstructor
public class DataImportService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataImportService.class);
    private final CityRepository cityRepository;

    @Override
    public void run(String... args) {
        if (cityRepository.count() > 0) {
            log.info("City data already exists. Skipping CSV import.");
            return;
        }

        log.info("Importing city data from classpath resource: uscities.csv");
        try (CSVReader reader = new CSVReader(new InputStreamReader(new ClassPathResource("uscities.csv").getInputStream()))) {
            reader.readNext(); // Skip header row
            String[] line;
            while ((line = reader.readNext()) != null) {
                City city = City.builder()
                        .name(line[0])
                        .state(line[2])
                        .build();
                cityRepository.save(city);
            }
            log.info("City data import complete. Total cities loaded: {}", cityRepository.count());
        } catch (IOException | CsvValidationException e) {
            log.error("Failed to import city data from CSV file.", e);
        }
    }
}