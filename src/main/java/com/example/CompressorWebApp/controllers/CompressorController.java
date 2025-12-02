package com.example.CompressorWebApp.controllers;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CompressorController {



    @GetMapping("/compressor")
    public String Compressor(Model model) {
        return "compressor";
    }
}
