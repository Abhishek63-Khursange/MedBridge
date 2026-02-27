package com.MedBridge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class MedBridge {

	public static void main(String[] args) {
		SpringApplication.run(MedBridge.class, args);
	}

}
