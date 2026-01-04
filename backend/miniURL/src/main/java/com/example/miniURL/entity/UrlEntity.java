package com.example.miniURL.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity //not just class but an entity that will be mapped to a database table
@Table(name = "url_entity")
public class UrlEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column
    private String mainUrl;

    @Column
    private String shortCode;
}
