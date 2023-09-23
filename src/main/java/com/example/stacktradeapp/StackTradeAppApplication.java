package com.example.stacktradeapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(scanBasePackages = {
		"com.example.stacktradeapp.enums",
		"com.example.stacktradeapp.controllers",
		"com.example.stacktradeapp.models",
		"com.example.stacktradeapp.jira",
		"com.example.stacktradeapp.milvus",
		"com.example.stacktradeapp.sentenceTransformers",
		"com.example.stacktradeapp.repositories",
		"com.example.stacktradeapp.security",
		"com.example.stacktradeapp.services",

})
public class StackTradeAppApplication {
	public static void main(String[] args) {
		SpringApplication.run(StackTradeAppApplication.class, args);

	}

}
