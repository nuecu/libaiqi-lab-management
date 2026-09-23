package org.example.libaiqi2023021108;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
@EnableAsync
public class Libaiqi2023021108Application {

    public static void main(String[] args) {
        SpringApplication.run(Libaiqi2023021108Application.class, args);
    }

}
