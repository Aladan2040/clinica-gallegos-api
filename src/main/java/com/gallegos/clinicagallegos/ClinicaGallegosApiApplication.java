package com.gallegos.clinicagallegos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ClinicaGallegosApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClinicaGallegosApiApplication.class, args);
    }

}
