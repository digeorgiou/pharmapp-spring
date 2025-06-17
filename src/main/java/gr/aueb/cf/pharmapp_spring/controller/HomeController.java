package gr.aueb.cf.pharmapp_spring.controller;

import org.springframework.web.bind.annotation.GetMapping;

public class HomeController {

    @GetMapping("/")
    public String showHomePage() {
        return "index"; // Returns the index.html template
    }
}
