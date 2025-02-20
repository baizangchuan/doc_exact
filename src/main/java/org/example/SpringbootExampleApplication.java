package org.example;

//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//
//@SpringBootApplication
//public class SpringbootExampleApplication {
//
//    public static void main(String[] args) {
//        SpringApplication.run(SpringbootExampleApplication.class, args);
//    }
//}


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class SpringbootExampleApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(SpringbootExampleApplication.class);

        application.addListeners((ApplicationListener<ServletWebServerInitializedEvent>) event -> {
            ConfigurableEnvironment environment = event.getApplicationContext().getEnvironment();
            Map<String, Object> map = new HashMap<>();
            map.put("server.port", 8083);
            environment.getPropertySources().addFirst(new MapPropertySource("customProperties", map));
        });

        application.run(args);
    }
}

