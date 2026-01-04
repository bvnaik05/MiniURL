package com.example.miniURL.service;

import com.example.miniURL.repository.UrlRepository;
import com.example.miniURL.util.UrlUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UrlService {
    private final UrlRepository urlRepository;
    private final UrlUtils urlUtils;

    public String shortenUrl(String url){
        //validate URL
        boolean isValid = urlUtils.isValid(url);
        if(!isValid){
            throw new RuntimeException("URL is invalid");
        }

        //generate unique short code
        String shorteCode = "TODO";
        //urlRepository.save();

        //persist to database

        //return meaningful data
        return null;
    }


}
