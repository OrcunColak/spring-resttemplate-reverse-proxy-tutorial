package com.colak.springtutorial.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
@Slf4j
public class RestTemplateConfig {

    // ClientHttpRequestFactory implementation that uses Apache HttpComponents HttpClient  to create requests.
    // @Bean
    public RestTemplate timeoutRestTemplate() {
        var timeout = 5_000;

        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory  = new HttpComponentsClientHttpRequestFactory();
        clientHttpRequestFactory.setConnectTimeout(timeout);
        clientHttpRequestFactory.setConnectionRequestTimeout(timeout);

        return new RestTemplate(clientHttpRequestFactory);
    }

    // ClientHttpRequestFactory implementation that uses standard JDK facilities.
    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(3000);
        return factory;
    }

    @Bean
    public RestTemplate restTemplate(ClientHttpRequestFactory clientHttpRequestFactory) {
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);

        // Add message converters
        List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
        messageConverters.add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        messageConverters.add(1, new MappingJackson2HttpMessageConverter());

        return restTemplate;
    }

}
