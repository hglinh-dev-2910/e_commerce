package com.sparkminds.ecommerce;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class TestRunner implements CommandLineRunner {

    @Value("${DB_URL:NOT_FOUND}")
    private String dbUrl;

    @Override
    public void run(String... args) {
        System.out.println("DB_URL = " + dbUrl);
    }


    @Test
    void contextLoads() {
        run();
    }
}
