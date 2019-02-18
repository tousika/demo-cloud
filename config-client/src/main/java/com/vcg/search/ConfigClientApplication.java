package com.vcg.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication @EnableEurekaClient @RestController @RefreshScope public class ConfigClientApplication {

    private final static Logger LOGGER = LoggerFactory.getLogger(ConfigClientApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ConfigClientApplication.class, args);
    }

    @Value("${name}")
    String name;

    @RequestMapping(value = "/hi") public String hi() {
        return "name: " + name;
    }

    @RequestMapping(value = "/name") public String name(String name) {
        return "your input: " + name;
    }
}

