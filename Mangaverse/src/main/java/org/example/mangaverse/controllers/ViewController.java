package org.example.mangaverse.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ViewController {

    @RequestMapping("/")
    public String mangaverse() {
        return "mangaverse.html";
    }
}
