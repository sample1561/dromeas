package edu.pec.dromeas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;

//TODO add Swagger for API Documentation

//TODO enable SSL for HTTPS requests
//  - Map HTTP to HTTPS

//TODO add scheduled task for checking for a clean scratch folder




@SpringBootApplication
@Configuration
public class DromeasApplication
{
    public static void main(String[] args) {
        SpringApplication.run(DromeasApplication.class, args);
    }
}


