package com.example.miniURL.repository;

import com.example.miniURL.entity.UrlEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UrlRepository extends JpaRepository<UrlEntity, Long> {
}
