package com.pelosa.rasutoda.controller.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.pelosa.rasutoda.dto.UserRegisterRequestDto;
import com.pelosa.rasutoda.service.UserService;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserApiController {
    private final UserService userService;
    private static final Logger log = LoggerFactory.getLogger(UserApiController.class);

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute UserRegisterRequestDto requestDto,
                               BindingResult bindingResult, // 2. BindingResult 파라미터 추가
                               Model model,
                               RedirectAttributes redirectAttributes) {

        // 3. 유효성 검사 실패 시 처리 로직 추가
        if (bindingResult.hasErrors()) {
            // 어떤 에러가 발생했는지 로그로 확인
            log.warn("회원가입 유효성 검사 오류: {}", bindingResult.getAllErrors());

            model.addAttribute("errorMessage", "회원가입 정보를 확인해주세요.");
            // 첫 번째 에러 메시지를 사용자에게 전달
            model.addAttribute("userRegisterRquestDto", requestDto);
            return "register"; // 다시 회원가입 페이지로
        }

        try {
            userService.register(requestDto);
        } catch (IllegalArgumentException e) {
            log.warn("회원가입 실패 (중복 등): {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/register";
        }
        redirectAttributes.addFlashAttribute("successMessage", "회원가입 완료입니다.");
        return "redirect:/login";

    }
}