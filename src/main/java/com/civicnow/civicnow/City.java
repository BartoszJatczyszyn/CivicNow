package com.civicnow.civicnow;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cities")
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class City {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 2) // States are usually 2 letters
    private String state;
}