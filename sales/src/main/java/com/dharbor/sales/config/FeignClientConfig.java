package com.dharbor.sales.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignClientConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("X-TENANT-ID", "DHBO");
            requestTemplate.header("X-CLIENT-ID", "SALES");
        };
    }
}
