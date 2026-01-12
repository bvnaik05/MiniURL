package com.example.miniURL.controller;

import com.example.miniURL.dto.ShortenUrlRequestDto;
import com.example.miniURL.dto.ShortenUrlResponseDto;
import com.example.miniURL.service.UrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;


@RestController
@RequiredArgsConstructor
public class UrlController {
    private final UrlService urlService;

    //idempotency: the ability of API to produce same result for same request
    @PostMapping("/shorten") // non-idempotent
    public ShortenUrlResponseDto shortenURL(@RequestBody ShortenUrlRequestDto requestDto){
        return urlService.shortenUrl(requestDto);
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> getRedirectionUrl(@PathVariable String shortCode) {
        URI redirectUri = urlService.getRedirectionUri(shortCode);
        return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                .location(redirectUri)
                .build();
    }
}
