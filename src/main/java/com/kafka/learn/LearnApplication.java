package com.kafka.learn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class LearnApplication {

    //TODO
    //  make postman requests
    //

    public static void main(String[] args) {
        SpringApplication.run(LearnApplication.class, args);
    }

}
