package com.nhonguoiyeucu.openlinkedhub;

import org.hibernate.cfg.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class OpenLinkedHubApplication {

    @Value("${server.port:8080}")
    private int serverPort;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    public static void main(String[] args) {
        SpringApplication.run(OpenLinkedHubApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void printSwaggerUrl() {
        String base = "http://localhost:" + serverPort + (contextPath == null ? "" : contextPath);
        System.out.println("\n==============================================");
        System.out.println("Swagger UI: " + base + "/swagger-ui/index.html");
        System.out.println("==============================================\n");
    }

}