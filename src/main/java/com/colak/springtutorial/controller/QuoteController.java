package com.colak.springtutorial.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class QuoteController {

    // http://localhost:8080/quote
    @GetMapping(path = "quote")
    public String quote() {
        return "test quote";
    }

}
