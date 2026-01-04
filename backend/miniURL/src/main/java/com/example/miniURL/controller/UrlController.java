package com.example.miniURL.controller;

import com.example.miniURL.service.UrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class UrlController {
    private final UrlService urlService;

    //idempotency: the ability of API to produce same result for same request
    @PostMapping // non-idempotent
    public String shortenURL(String url){
        return urlService.shortenUrl(url);
    }
}
