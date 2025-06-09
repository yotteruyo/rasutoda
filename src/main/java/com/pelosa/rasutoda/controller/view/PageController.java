package com.pelosa.rasutoda.controller.view;

import com.pelosa.rasutoda.dto.PartyDto;
import com.pelosa.rasutoda.dto.OttOption;
import com.pelosa.rasutoda.dto.PartyCreateForm;
import org.springframework.stereotype.Controller;
import com.pelosa.rasutoda.service.PartyServices;
import org.springframework.ui.Model;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
    public String showRegisterPage() {return "register";}

    @GetMapping("/faq")
    public String ShowFaqPage() { return "faq";}

    @GetMapping("/contact")
    public String showContactPage() { return "contact";}

    @GetMapping("/privacy")
    public String showPrivacyPage() { return "privacy";}

    @GetMapping("/support")
    public String showSupportPage() { return "support";}

    @GetMapping("/terms")
    public String showTremsPage(){return "terms";}

    @GetMapping("/mypage")
    public String showMypage() { return "mypage";}

    @GetMapping("/mypage/profile-edit")
    public String showProfileedit() {return "profile-edit";}

    @GetMapping("/mypage/password-change")
    public String showPasswordChange() {return "passwordchange";}

    @GetMapping("/mypage/contact")
    public String showContact() { return "contact";}


    @GetMapping("/party-create")
    public String showPartyCreateForm(@RequestParam(value = "ott", required = false) String ottName, Model model) {
        List<OttOption> ottList = Arrays.asList(
                new OttOption("netflix", "넷플릭스"),
                new OttOption("youtube", "유튜브 프리미엄"),
                new OttOption("disneyplus", "디즈니플러스"),
                new OttOption("wavve", "웨이브"),
                new OttOption("tving", "티빙"),
                new OttOption("coupangplay", "쿠팡 플레이"),
                new OttOption("watcha", "왓챠")
                );

        model.addAttribute("ottList", ottList);

        model.addAttribute("selectedOtt", Optional.ofNullable(ottName)
                .map(name -> {
                    if (name.equalsIgnoreCase("netflix")) return "넷플릭스";
                    if (name.equalsIgnoreCase("youtube")) return "유튜브 프리미엄";
                    if (name.equalsIgnoreCase("disneyplus")) return "디즈니+";
                    if (name.equalsIgnoreCase("wavve")) return "웨이브";
                    if (name.equalsIgnoreCase("tving")) return "티빙";
                    if (name.equalsIgnoreCase("coupangplay")) return "쿠팡 플레이";
                    if (name.equalsIgnoreCase("watcha")) return "왓챠";
                    return name.substring(0, 1).toUpperCase() + name.substring(1);
                })
                .orElse(null));
        return "party-create";
    }

    @PostMapping("/api/parties/create")
    public String createParty(@ModelAttribute PartyCreateForm partyForm, Model model) {

        partyService.createParty(partyForm);

        System.out.println("--- 파티 생성 요청 데이터 ---");
        System.out.println("OTT 이름: " + partyForm.getOttName());
        System.out.println("파티 제목: " + partyForm.getPartyTitle());
        System.out.println("남은 일수: " + partyForm.getRemainingDays());
        System.out.println("모집 인원: " + partyForm.getMaxMembers());
        System.out.println("월 가격: " + partyForm.getMonthlyPrice());
        System.out.println("--------------------------");

        return "redirect:/mypage"; // + partyForm.getOttName();
    }

    @GetMapping("/party-list")
    public String partiesPage(@RequestParam(name= "ott", required = false) String ott, Model model) {
        List<PartyDto> parties;
        if (ott != null && !ott.isBlank()){
            parties = partyService.findPartiesByOtt(ott);
            model.addAttribute("selectedOtt", Optional.ofNullable(ott)
                    .map(name -> {
                        if (name.equalsIgnoreCase("netflix")) return "넷플릭스";
                        if (name.equalsIgnoreCase("youtube")) return "유튜브 프리미엄";
                        if (name.equalsIgnoreCase("disneyplus")) return "디즈니+";
                        if (name.equalsIgnoreCase("wavve")) return "웨이브";
                        if (name.equalsIgnoreCase("tving")) return "티빙";
                        if (name.equalsIgnoreCase("coupangplay")) return "쿠팡 플레이";
                        if (name.equalsIgnoreCase("watcha")) return "왓챠";
                        return name.substring(0, 1).toUpperCase() + name.substring(1);
                    })
                    .orElse(null));
        } else {
            parties = partyService.findAllParties();
            model.addAttribute("selectedOtt", null);
        }


        model.addAttribute("parties", parties);

        return "party-list";
    }

}
