package com.colak.springtutorial.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ProxyController {

    private final RestTemplate restTemplate;

    @GetMapping("/favicon.ico")
    public ResponseEntity<Void> favicon() {
        return ResponseEntity.notFound().build();
    }

    // http://localhost:8080/forward/quote
    // http://localhost:8080/forward/quote1
    @RequestMapping(value = "forward/**",
            method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public ResponseEntity<?> proxyRequest(HttpServletRequest request) throws URISyntaxException, IOException {
        // Skip favicon.ico requests
        if (request.getRequestURI().equals("/favicon.ico")) {
            return ResponseEntity.notFound().build();
        }

        String host = "localhost";
        int port = 8080;
        int length = "forward".length() + 1;
        String substring = request.getRequestURI().substring(length);

        String targetUrl = String.format("http://%s:%d%s",
                host,
                port,
                substring
        );

        // Add query string if present
        String queryString = request.getQueryString();
        if (queryString != null) {
            targetUrl += "?" + queryString;
        }

        log.info("Forwarding request to: {}", targetUrl);

        try {
            HttpEntity<byte[]> httpEntity = createHttpEntity(request);

            // Forward the request
            ResponseEntity<String> response = restTemplate.exchange(
                    new URI(targetUrl),
                    HttpMethod.valueOf(request.getMethod()),
                    httpEntity,
                    String.class
            );

            // Copy response headers
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.putAll(response.getHeaders());

            return new ResponseEntity<>(response.getBody(), responseHeaders, response.getStatusCode());
        } catch (HttpClientErrorException.NotFound exception) {
            log.warn("Resource not found at {}: {}", targetUrl, exception.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception exception) {
            log.error("Error forwarding request to {}: {}", targetUrl, exception.getMessage());
            throw exception;
        }
    }

    private static HttpEntity<byte[]> createHttpEntity(HttpServletRequest request) throws IOException {
        // Copy headers
        HttpHeaders headers = copyHttpHeaders(request);

        // Get the request body if present
        byte[] body = StreamUtils.copyToByteArray(request.getInputStream());

        // Create the entity with headers and body
        return new HttpEntity<>(body, headers);
    }

    private static HttpHeaders copyHttpHeaders(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            List<String> headerValues = Collections.list(request.getHeaders(headerName));
            headers.addAll(headerName, headerValues);
        }
        return headers;
    }
}
