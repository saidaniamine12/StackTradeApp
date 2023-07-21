package com.example.stacktradeapp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(scanBasePackages = {"com.example.stacktradeapp.mongodb",
		"com.example.stacktradeapp.elasticsearch"
,"com.example.stacktradeapp.controllers"
,"com.example.stacktradeapp.entities",
"com.example.stacktradeapp.config",
"com.example.stacktradeapp.milvus",
"com.example.stacktradeapp.models",
})
public class StackTradeAppApplication {
	public static final Logger logger = LogManager.getLogger(StackTradeAppApplication.class);
	public static void main(String[] args) {
		SpringApplication.run(StackTradeAppApplication.class, args);

	}

}
