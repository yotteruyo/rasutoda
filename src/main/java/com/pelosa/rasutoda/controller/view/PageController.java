package com.pelosa.rasutoda.controller.view;

import com.pelosa.rasutoda.domain.User;

import com.pelosa.rasutoda.dto.*;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import com.pelosa.rasutoda.service.PartyServices;
import com.pelosa.rasutoda.service.UserService;
import com.pelosa.rasutoda.service.PaymentsService;
import org.springframework.ui.Model;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class PageController {

    private final PartyServices partyService;
    private final UserService userService;
    private final PaymentsService paymentsService;

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

    @GetMapping("/party/join/{partyId}")
    public String showMyJoinPartyPage(@PathVariable Long partyId ,Model model){
        PartyDto partyDto = partyService.findPartyDetailById(partyId)
                .orElseThrow(() -> new IllegalArgumentException("파티를 찾을 수 없습니다: " + partyId));

        model.addAttribute("party", partyDto);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loginId = authentication.getName();;
        User currentUser = userService.findUserByLoginId(loginId)
                .orElseThrow(() -> new IllegalStateException("로그인된 사용자를 찾을 수 없습니다: " + loginId));
        model.addAttribute("currentUser", currentUser);

        return "join";
    }

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
    public String showMypage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loginId = authentication.getName();

        User currentUser = userService.findUserByLoginId(loginId)
                .orElseThrow(() -> new IllegalStateException("로그인된 사용자를 찾을 수 없습니다: " + loginId));
        model.addAttribute("user", currentUser);

        List<PartyDto> createdParties = partyService.findCreatedPartiesByCreator(currentUser);
        model.addAttribute("createdParties", createdParties);

        List<PartyDto> joinedParties = partyService.findJoinedPartiesByUser(currentUser);
        model.addAttribute("joinedParties", joinedParties);

        return "mypage";}

    @GetMapping("/mypage/profile-edit")
    public String showProfileedit(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loginId = authentication.getName();

        User currentUser = userService.findUserByLoginId(loginId)
                .orElseThrow(() -> new IllegalStateException("로그인된 사용자를 찾을 수 없습니다: " + loginId));

        UserProfileUpdateDto profileDto = new UserProfileUpdateDto(
                currentUser.getNickname(),
                currentUser.getEmail(),
                currentUser.getPhoneNumber()
        );
        model.addAttribute("profileDto", profileDto);

        return "profile-edit";}

    @PostMapping("/mypage/profile-update")
    public String updateProfile(@Valid @ModelAttribute UserProfileUpdateDto profileDto, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", "회원 정보 수정에 실패했습니다. 입력 값을 확인해주세요.");
            model.addAttribute("profileDto", profileDto);
            return "profile-edit";
        }

        try {
            userService.profileEdit(profileDto);
            redirectAttributes.addFlashAttribute("successMessage", "회원 정보가 성공적으로 수정되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/mypage/profile-edit";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "사용자 정보 처리 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/mypage/profile-edit";
        }
        return "redirect:/mypage";
    }

    @GetMapping("/mypage/password-change")
    public String showPasswordChange(Model model) {

        model.addAttribute("passwordDto", new PasswordUpdateDto());


        return "password-change";}

    @PostMapping("/mypage/password-update")
    public String updatePassword(@Valid @ModelAttribute PasswordUpdateDto passwordDto, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", "회원 정보 수정에 실패했습니다. 입력 값을 확인해주세요.");
            model.addAttribute("passwordDto", passwordDto);
            return "password-change";
        }

        try {
            userService.passwordUpdate(passwordDto);
            redirectAttributes.addFlashAttribute("successMessage", "회원 정보가 성공적으로 수정되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/mypage/password-change";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "사용자 정보 처리 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/mypage/password-change";
        }
        return "redirect:/";}


    @GetMapping("/mypage/my-party/{partyId}")
    public String showMyPartyDetail(@PathVariable Long partyId, Model model){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loginId = authentication.getName();
        User currentUser = userService.findUserByLoginId(loginId)
                .orElseThrow(()  -> new IllegalStateException("로그인된 사용자를 찾을 수 없습니다." + loginId));
        model.addAttribute("currentUser", currentUser);

        PartyDto myPartyDetail = partyService.findMyPartyDetailForUser(partyId, currentUser)
                .orElseThrow(() -> new IllegalArgumentException("파티를 찾을 수 없거나 접근 권한이 없습니다: " + partyId));

        model.addAttribute("myPartyDetail", myPartyDetail);
        model.addAttribute("user", currentUser);
        return "myparty";
    }


    @GetMapping("/party-create")
    public String showPartyCreateForm(@RequestParam(value = "ott", required = false) String ottName, Model model) {
        List<OttOption> ottList = Arrays.asList(
                new OttOption("netflix", "넷플릭스"),
                new OttOption("youtube", "유튜브 프리미엄"),
                new OttOption("disneyplus", "디즈니플러스"),
                new OttOption("wavve", "웨이브"),
                new OttOption("tving", "티빙"),
                new OttOption("coupangplay", "쿠팡 플레이")
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

        return "redirect:/mypage";
    }

    @GetMapping("/payment/success")
    public String showPaymentSuccessPage(
            @RequestParam String paymentType,
            @RequestParam String orderId,
            @RequestParam String paymentKey,
            @RequestParam Long amount,
            Model model) {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String loginId = authentication.getName();
            User currentUser = userService.findUserByLoginId(loginId)
                    .orElseThrow(() -> new IllegalStateException("로그인된 사용자를 찾을 수 없습니다: " + loginId));

            paymentsService.processPaymentSuccess(paymentType, orderId, paymentKey, amount, currentUser);

            model.addAttribute("message", "결제가 성공적으로 완료되어 파티에 참여되었습니다!");
            model.addAttribute("orderId", orderId); // HTML에서 partyId 추출용

            return "payment-success"; // payment-success.html 템플릿 렌더링
        } catch (IllegalArgumentException e) { // 파티 ID 추출 실패, 유효하지 않은 파티, 이미 참여 등
            System.err.println("결제 성공 페이지 처리 중 비즈니스 로직 오류: " + e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            // 에러 발생 시 결제 실패 페이지로 이동
            return "redirect:/payment/fail?code=BUSINESS_ERROR&message=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
        } catch (IllegalStateException e) { // 로그인 유저 없거나 등 (Authentication/Authorization 문제)
            System.err.println("결제 성공 페이지 처리 중 인증/상태 오류: " + e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/login"; // 로그인 페이지로 리다이렉트
        } catch (Exception e) {
            System.err.println("결제 성공 페이지 처리 중 알 수 없는 예외 발생: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "결제 처리 중 시스템 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            return "redirect:/payment/fail";
        }
    }

    @GetMapping("/payment/fail")
    public String showPaymentFailPage(@RequestParam String code, @RequestParam String message, Model model) {
        model.addAttribute("errorMessage", "결제가 실패했습니다: " + message + " (코드: " + code + ")");
        return "payment-fail";
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
