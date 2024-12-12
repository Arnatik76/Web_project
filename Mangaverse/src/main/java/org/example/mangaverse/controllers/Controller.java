package org.example.mangaverse.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/read")
public class Controller {

    @GetMapping("/stalker_3/a")
    public String stalker_3_a() {
        return "read";
    }
}
