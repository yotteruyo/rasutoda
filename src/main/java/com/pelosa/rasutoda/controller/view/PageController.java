package com.pelosa.rasutoda.controller.view;

import com.pelosa.rasutoda.domain.User;

import com.pelosa.rasutoda.dto.*;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User; // 임포트 추가
import org.springframework.stereotype.Controller;
import com.pelosa.rasutoda.service.PartyServices;
import com.pelosa.rasutoda.service.UserService;
import com.pelosa.rasutoda.service.PaymentsService;
import com.pelosa.rasutoda.dto.OAuth2AdditionalInfoRequestDto;
// import com.pelosa.rasutoda.repository.UserRepository; // PageController에서 직접 UserRepository를 사용하지 않으므로 제거 가능

import org.springframework.ui.Model;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken; // 임포트 추가
import org.springframework.security.core.userdetails.UserDetails; // 임포트 추가 (일반 유저)


import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class PageController {

    private final PartyServices partyService;
    private final UserService userService;
    private final PaymentsService paymentsService;

    // currentUser를 가져오는 헬퍼 메서드
    private User getCurrentUserFromAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("현재 로그인된 사용자 정보가 없습니다.");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) { // 자체가입
            String loginId = ((UserDetails) principal).getUsername();
            return userService.findUserByLoginId(loginId)
                    .orElseThrow(() -> new IllegalStateException("로그인된 사용자(일반)를 찾을 수 없습니다: " + loginId));
        } else if (principal instanceof OAuth2User) { // OAuth2가입
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            String providerId = oauthToken.getName();
            String provider = oauthToken.getAuthorizedClientRegistrationId();

            return userService.findByProviderAndProviderId(provider, providerId)
                    .orElseThrow(() -> new IllegalStateException("OAuth2 사용자(소셜)를 찾을 수 없습니다: " + providerId));
        } else {
            throw new IllegalStateException("알 수 없는 인증 주체 타입입니다. 사용자 정보를 가져올 수 없습니다.");
        }
    }


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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = getCurrentUserFromAuthentication(authentication);

        PartyDto partyDto = partyService.findPartyDetailById(partyId)
                .orElseThrow(() -> new IllegalArgumentException("파티를 찾을 수 없습니다: " + partyId));

        model.addAttribute("party", partyDto);
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
        User currentUser = getCurrentUserFromAuthentication(authentication);

        model.addAttribute("user", currentUser);

        List<PartyDto> createdParties = partyService.findCreatedPartiesByCreator(currentUser);
        model.addAttribute("createdParties", createdParties);

        List<PartyDto> joinedParties = partyService.findJoinedPartiesByUser(currentUser);
        model.addAttribute("joinedParties", joinedParties);

        return "mypage";}

    @GetMapping("/mypage/profile-edit")
    public String showProfileedit(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = getCurrentUserFromAuthentication(authentication);

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
            return "redirect:/";
        }
        return "redirect:/";}

    @GetMapping("/mypage/my-party/{partyId}/edit-account")
    public String showPartyAccountEditForm(@PathVariable Long partyId, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = getCurrentUserFromAuthentication(authentication); // 헬퍼 메서드 사용

        PartyDto partyDetail = partyService.findMyPartyDetailForUser(partyId, currentUser)
                .orElseThrow(() -> new IllegalArgumentException("파티를 찾을 수 없거나 수정 권한이 없습니다: " + partyId));

        model.addAttribute("partyId", partyId);
        model.addAttribute("editForm", new PartyUpdateAccountInfoRequestDto());
        model.addAttribute("ottName", partyDetail.getOttName());

        if (model.containsAttribute("errorMessage")) {
            model.addAttribute("errorMessage", model.asMap().get("errorMessage"));
        }
        if (model.containsAttribute("successMessage")) {
            model.addAttribute("successMessage", model.asMap().get("successMessage"));
        }

        return "partyUpdate";
    }

    @PostMapping("/api/party/update/{partyId}")
    public String updatePartyAccountInfo(
            @PathVariable Long partyId, @Valid @ModelAttribute PartyUpdateAccountInfoRequestDto accountInfoRequestDto,
            BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "OTT 계정 정보 입력이 올바르지 않습니다.");
            return "redirect:/mypage/my-party/" + partyId;
        }

        try {
            partyService.updatePartyAccountInfo(accountInfoRequestDto, partyId);
            redirectAttributes.addFlashAttribute("successMessage", "OTT 계정 정보가 성공적으로 업데이트되었습니다!");
        } catch (IllegalAccessException e) {
            System.err.println("파티 정보 수정 권한 오류: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("파티 정보 업데이트 인자 오류: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (IllegalStateException e) {
            System.err.println("파티 정보 업데이트 상태 오류: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            System.err.println("파티 정보 업데이트 중 알 수 없는 오류: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "파티 정보 업데이트 중 알 수 없는 오류가 발생했습니다.");
        }

        return "redirect:/mypage/my-party/" + partyId;
    }

    @GetMapping("/mypage/my-party/{partyId}")
    public String showMyPartyDetail(@PathVariable Long partyId, Model model){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = getCurrentUserFromAuthentication(authentication);
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
            User currentUser = getCurrentUserFromAuthentication(authentication);

            paymentsService.processPaymentSuccess(paymentType, orderId, paymentKey, amount, currentUser);

            model.addAttribute("message", "결제가 성공적으로 완료되어 파티에 참여되었습니다!");
            model.addAttribute("orderId", orderId);

            return "payment-success";
        } catch (IllegalArgumentException e) { // 파티 ID 추출 실패, 유효하지 않은 파티, 이미 참여 등
            System.err.println("결제 성공 페이지 처리 중 비즈니스 로직 오류: " + e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/payment/fail?code=BUSINESS_ERROR&message=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
        } catch (IllegalStateException e) {
            System.err.println("결제 성공 페이지 처리 중 인증/상태 오류: " + e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/login";
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

    @GetMapping("/register/oauth2-additional-info")
    public String showOAuth2AdditionalInfoPage(Model model, Authentication authentication) {
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            OAuth2User oauth2User = oauthToken.getPrincipal();

            String email = oauth2User.getAttribute("email");
            String nickname = null;
            if ("kakao".equals(oauthToken.getAuthorizedClientRegistrationId())) {
                Map<String, Object> kakaoAccount = (Map<String, Object>) oauth2User.getAttributes().get("kakao_account");
                Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
                nickname = (String) profile.get("nickname");
            } else if ("google".equals(oauthToken.getAuthorizedClientRegistrationId())) {
                nickname = (String) oauth2User.getAttribute("name");
            }

            OAuth2AdditionalInfoRequestDto dto = new OAuth2AdditionalInfoRequestDto(nickname != null ? nickname : "", null);
            model.addAttribute("additionalInfoDto", dto);
            model.addAttribute("oauth2Email", email);

            return "oauth2-additional-info";
        }
        return "redirect:/";
    }

    @PostMapping("/api/register/oauth2-complete")
    public ResponseEntity<Map<String, String>> completeOAuth2Registration(
            @Valid @RequestBody OAuth2AdditionalInfoRequestDto additionalInfoDto,
            BindingResult bindingResult,
            Authentication authentication) {

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(Map.of("message", "입력 값을 확인해주세요."));
        }

        if (!(authentication instanceof OAuth2AuthenticationToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "OAuth2 로그인 상태가 아닙니다."));
        }

        try {
            userService.completeOAuth2Registration(additionalInfoDto, authentication);
            return ResponseEntity.ok(Map.of("message", "회원가입이 완료되었습니다!", "redirectUrl", "/"));
        } catch (Exception e) {
            System.err.println("추가 정보 저장 실패: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "추가 정보 저장 실패: " + e.getMessage()));
        }
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