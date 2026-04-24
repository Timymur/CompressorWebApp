package com.example.CompressorWebApp.controllers;

import com.example.CompressorWebApp.models.Compressor;
import com.example.CompressorWebApp.models.Station;
import com.example.CompressorWebApp.models.User;

import com.example.CompressorWebApp.services.CompressorService;
import com.example.CompressorWebApp.services.StationService;
import com.example.CompressorWebApp.services.UserService;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

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
    public String Home(Model model, HttpSession session) {

        User user = userService.GetCurrentUser();
        if (user == null) return "auth";

        model.addAttribute("user", user);
        Station station = null;

        if(user.getRole().equals("admin")){
            Long selectedStationId = (Long) session.getAttribute("selectedStationId");
            if (selectedStationId == null) {
                return "redirect:/select-station";
            }
            Optional<Station> optStation = stationService.findById(selectedStationId);
            if (optStation.isPresent()) {
                station = optStation.get();
            } else {

                session.removeAttribute("selectedStationId");
                return "redirect:/select-station";
            }
        }else{
            station = user.getStation();
            if (station == null) {
                model.addAttribute("nullStation", "Вы не относитесь ни к одной станции");
                return "auth";
            }
        }


        model.addAttribute("station", station);


        List<User> workers = userService.findByStationId(station.getId());

        model.addAttribute("workers", workers);

        List<Compressor> compressors = compressorService.findByStationId(station.getId());

        if(compressors == null) model.addAttribute("nullCompressors", "На этой станции нет компрессоров");
        model.addAttribute("compressors", compressors);

        Optional<User> onShiftUser = workers.stream()
                .filter(User::isInWork)
                .findFirst();

        model.addAttribute("onShiftUser", onShiftUser.orElse(null));

        return "main";
    }

    @GetMapping("/logout")
    public String Logout(Model model) {
        model.addAttribute("title", "Выход");
        return "logout";
    }

    @GetMapping("/select-station")
    public String selectStation(Model model, HttpSession session) {
        User user = userService.GetCurrentUser();
        if (user == null) return "auth";

        if (!user.getRole().equals("admin")) {
            return "redirect:/";
        }

        List<Station> stations = stationService.findAll();
        model.addAttribute("stations", stations);
        return "select-station";
    }

    @PostMapping("/select-station")
    public String selectStationPost(@RequestParam Long stationId, HttpSession session) {
        User user = userService.GetCurrentUser();
        if (user == null) return "auth";

        if (!user.getRole().equals("admin")) {
            return "redirect:/";
        }

        session.setAttribute("selectedStationId", stationId);
        return "redirect:/";
    }


}
