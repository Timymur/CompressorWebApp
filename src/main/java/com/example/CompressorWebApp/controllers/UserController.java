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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Controller
public class UserController {


    private final UserService userService;
    private final StationService stationService;
    private final CompressorService compressorService;


    public UserController(UserService userService, StationService stationService, CompressorService compressorService) {
        this.userService = userService;
        this.stationService = stationService;
        this.compressorService = compressorService;
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

    @GetMapping("/openCloseShift")
    public String openCloseShift(Model model) {

        User user = userService.GetCurrentUser();
        if (user == null) return "auth";

        Station station = user.getStation();
        if (station == null) {
            model.addAttribute("nullStation", "Вы не относитесь ни к одной станции");
            return "auth";
        }
        model.addAttribute("station", station);

        List<Compressor> compressors = compressorService.findByStationId(station.getId());

        if(compressors == null) model.addAttribute("nullCompressors", "На этой станции нет компрессоров");
        model.addAttribute("compressors", compressors);


        List<User> workers = userService.findByStationId(station.getId());
        if (user.isInWork()) {
            userService.closeShift(user);
        } else {
            boolean hasWorkerOnShift = workers.stream().anyMatch(User::isInWork);
            if (!hasWorkerOnShift) {
                userService.openShift(user);
            }
        }

        workers = userService.findByStationId(station.getId());


        Optional<User> onShiftUser = workers.stream()
                .filter(User::isInWork)
                .findFirst();

        model.addAttribute("user", user);
        model.addAttribute("workers", workers);
        model.addAttribute("onShiftUser", onShiftUser.orElse(null));

        return "main";
    }

}
