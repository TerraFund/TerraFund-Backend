package com.example.TerraFund;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(
		tags = {
				@Tag(name = "1. Authentication", description = "Auth-related endpoints"),
				@Tag(name = "2. Admin Portal", description = "Admin-related endpoints"),
				@Tag(name = "3. Land Portal", description = "Land-related endpoints")
		}
)

@SpringBootApplication
public class TerraFundApplication {

	public static void main(String[] args) {
		SpringApplication.run(TerraFundApplication.class, args);
	}

}
