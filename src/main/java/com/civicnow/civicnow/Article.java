package com.civicnow.civicnow;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "articles")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
// Use a business key for equals and hashCode to ensure consistency.
@EqualsAndHashCode(of = "url")
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 1024)
    private String url;

    @Column(length = 512)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String author;
    private String sourceName;
    private String urlToImage;
    private OffsetDateTime publishedAt;

    // Fields populated by the analysis service.
    private Boolean isLocal;
    private String cityName;
    private String stateAbbreviation;
}