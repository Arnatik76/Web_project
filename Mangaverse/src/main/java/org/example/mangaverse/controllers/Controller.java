package org.example.mangaverse.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping("/read")
public class Controller {

    @GetMapping("/stalker_3/a")
    public String stalker_3_a() {
        return "read";
    }


//    @GetMapping("/stalker_3/a")
//    public ModelAndView stalker_3_a(@RequestParam(name = "chapter", required = false) int chapter,
//                                    @RequestParam(name = "page", required = false) int page) {
//        ModelAndView modelAndView = new ModelAndView("read");
//        modelAndView.addObject("chapter", chapter);
//        modelAndView.addObject("page", page);
//        return modelAndView;
//    }
}
