package com.example.miniURL.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity //not just class but an entity that will be mapped to a database table
@Table(name = "url_entity", indexes = {
    @Index(name = "idx_short_code", columnList = "shortCode", unique = true)
})
public class UrlEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "main_url", length = 2048, nullable = false)
    private String mainUrl;

    @Column(name = "short_code", length = 8, nullable = false, unique = true)
    private String shortCode;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
