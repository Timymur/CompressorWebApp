package com.example.CompressorWebApp.controllers;

import com.example.CompressorWebApp.models.Compressor;
import com.example.CompressorWebApp.models.Station;
import com.example.CompressorWebApp.models.User;

import com.example.CompressorWebApp.services.CompressorService;
import com.example.CompressorWebApp.services.StationService;
import com.example.CompressorWebApp.services.UserService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class MainController {

    private final UserService userService;;
    private final CompressorService compressorService;
    private final StationService stationService;


    public MainController(UserService userService, CompressorService compressorService, StationService stationService ) {
        this.userService = userService;
        this.compressorService = compressorService;
        this.stationService = stationService;

    }

    @GetMapping("/")
    public String Home(Model model) {

        User user = userService.GetCurrentUser();
        if (user == null) return "auth";

        model.addAttribute("user", user);


        Station station = user.getStation();
        if (station == null) {
            model.addAttribute("nullStation", "Вы не относитесь ни к одной станции");
            return "home";
        }

        model.addAttribute("station", station);


        List<User> workers = userService.findByStationId(station.getId());

        model.addAttribute("workers", workers);

        List<Compressor> compressors = compressorService.findByStationId(station.getId());

        if(compressors == null) model.addAttribute("nullCompressors", "На этой станции нет компрессоров");
        model.addAttribute("compressors", compressors);

        return "main";
    }

    @GetMapping("/logout")
    public String Logout(Model model) {
        model.addAttribute("title", "Выход");
        return "logout";
    }


}
