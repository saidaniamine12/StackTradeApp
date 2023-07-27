package com.example.stacktradeapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(scanBasePackages = {"com.example.stacktradeapp.mongodb",
"com.example.stacktradeapp.enums",
"com.example.stacktradeapp.controllers",
"com.example.stacktradeapp.entities",
"com.example.stacktradeapp.config",
"com.example.stacktradeapp.milvus",
"com.example.stacktradeapp.models",
})
public class StackTradeAppApplication {
	private final Logger logger = LoggerFactory.getLogger(StackTradeAppApplication.class);
	public static void main(String[] args) {
		SpringApplication.run(StackTradeAppApplication.class, args);

	}

}
