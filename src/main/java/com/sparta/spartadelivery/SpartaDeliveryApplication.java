package com.sparta.spartadelivery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class SpartaDeliveryApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpartaDeliveryApplication.class, args);
    }

}
