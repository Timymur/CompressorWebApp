package com.example.CompressorWebApp.controllers;

import com.example.CompressorWebApp.models.Station;
import com.example.CompressorWebApp.models.User;
import com.example.CompressorWebApp.services.StationService;
import com.example.CompressorWebApp.services.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Controller
public class UserController {


    private final UserService userService;
    private final StationService stationService;


    public UserController(UserService userService, StationService stationService) {
        this.userService = userService;
        this.stationService = stationService;
    }

    @GetMapping("/registration")
    public String Registration(Model model) {

        List<Station> stations = stationService.findAll();
        model.addAttribute("stations", stations);

        return "registration";
    }

    @GetMapping("/auth")
    public String auth(Model model) {
        return "auth";
    }

    @PostMapping("/auth")
    public String authAct(Model model) {
        return "auth";
    }


    @PostMapping("/registration")
    public String registrationAct(@RequestParam String firstName, @RequestParam String secondName, @RequestParam String login,
                                  @RequestParam String password,  @RequestParam String confirmPassword,
                                  @RequestParam Long stationId,  @RequestParam String codeword, @RequestParam String jobTitle, Model model) {


        String lowerLogin = login.trim().toLowerCase(Locale.ROOT);
        if (userService.findByLogin(lowerLogin) != null) {
            model.addAttribute("errorMessage", "Пользователь с таким логином уже существует");
            return "registration";
        }
        Station station = stationService.findById(stationId)
                .orElseThrow(() -> new IllegalArgumentException("Станция не найдена"));

        if(!station.getCodeWord().equals(codeword)){
            model.addAttribute("errorMessage", "Неверное кодовое слово");
            return "registration";
        }

        if(!password.equals(confirmPassword)){
            model.addAttribute("errorMessage", "Пароли не совпадают");
            return "registration";
        }
        String role;
        if(jobTitle.equals("Машинист")) role = "user";
        else if(jobTitle.equals("Начальник")) role = "manager";
        else {
            model.addAttribute("errorMessage", "Неверная должность");
            return "registration";
        }

        User user = new User(firstName, secondName, lowerLogin, password,jobTitle, role, station);
        userService.registerUser(user);

        return "redirect:/authorization";


    }
}
