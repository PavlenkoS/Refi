package com.example.Refi.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationProperties
@PropertySource("classpath:dtf.properties")
@Getter
@Setter
public class DealTypeConfig {
    private String dealFieldTypes;
}
