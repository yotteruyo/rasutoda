package com.pelosa.rasutoda.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PageController {

    @GetMapping("/")
    public String showMainPage() {
        return "main";
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";
    }

    @GetMapping("/party/create/{ott}")
    public String showPartyCreateForm(@PathVariable String ott, Model model) {
        model.addAttribute("ottName", ott);
        return "party-create";
    }

    @GetMapping("/party/list/{ott}")
    public String showPartyListPage(@PathVariable String ott, Model model) {
        model.addAttribute("ottName", ott);
        return "party-list";
    }

}
