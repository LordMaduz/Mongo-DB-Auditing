package com.spring.mongo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "Mongo Auditing Service", version = "1.0", description = "Mongo Auditing Service"))
public class MongoChangeStreamsApplication {

	public static void main(String[] args) {
		SpringApplication.run(MongoChangeStreamsApplication.class, args);
	}

}
