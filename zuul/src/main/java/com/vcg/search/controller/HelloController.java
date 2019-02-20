package com.vcg.search.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by dongsijia on 2019/2/18.
 */
@RestController
public class HelloController {
    @RequestMapping("/local")
    public String hello() {
        return "hello api gateway";
    }
}
