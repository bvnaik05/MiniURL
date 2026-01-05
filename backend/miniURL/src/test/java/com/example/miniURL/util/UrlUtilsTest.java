package com.example.miniURL.util;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
public class UrlUtilsTest {
    @Autowired
    private UrlUtils urlUtils;
    @Test
    void test_isValid(){
        assertFalse(urlUtils.isValid(""));
    }
}
