package com.pelosa.rasutoda.controller.view;

import com.pelosa.rasutoda.dto.PartyDto;
import org.springframework.stereotype.Controller;
import com.pelosa.rasutoda.service.PartyServices;
import org.springframework.ui.Model;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class PageController {

    private final PartyServices partyService;

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

    @GetMapping("/party-list")
    public String partiesPage(@RequestParam(name= "ott", required = false) String ott, Model model) {
        List<PartyDto> parties;
        if (ott != null && !ott.isBlank()){
            parties = partyService.findPartiesByOtt(ott);
        } else {
            parties = partyService.findAllParties();
        }

        model.addAttribute("selectedOtt", ott);
        model.addAttribute("parties", parties);

        return "party-list";
    }

}
