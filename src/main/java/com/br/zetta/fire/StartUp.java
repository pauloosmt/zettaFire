package com.br.zetta.fire;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class StartUp{

	public static void main(String[] args) {
		SpringApplication.run(StartUp.class, args);
	}

}
