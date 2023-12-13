package io.kamenskiyAndrey.processingService.processing.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class CloudConfig {

//    @Bean
//    @LoadBalanced
//    public RestTemplateBuilder restTemplateBuilder() {
//        return new RestTemplateBuilder();
//    }

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return new RestTemplate();
    }
}
