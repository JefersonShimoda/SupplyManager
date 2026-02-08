package com.supplymanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.supplymanager")
@EntityScan(basePackages = "com.supplymanager.domain.model")
@EnableJpaRepositories(basePackages = "com.supplymanager.repository")
public class SupplyManagerApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(SupplyManagerApplication.class);

        app.addListeners((ApplicationReadyEvent event) -> {
            String port = event.getApplicationContext()
                    .getEnvironment().getProperty("local.server.port");

            System.out.println("🚀 Application started on the port: " + port);
        });

        app.run(args);
    }

}
