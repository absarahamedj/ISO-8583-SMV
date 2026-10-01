package com.payments.validator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class IsoValidatorApplication {
    public static void main(String[] args) {
        SpringApplication.run(IsoValidatorApplication.class, args);
    }
}
