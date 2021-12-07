package id.holigo.services.holigooauthservice.web.controllers;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class TestController {

    @GetMapping("/api/v1/test")
    public String test(HttpServletRequest request) {
        log.info("Test is run....");
        log.info("header user_id -> {}", request.getHeader("user_id"));    
        return "Test";
    }
}
