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
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            log.warn("회원가입 유효성 검사 오류: {}", bindingResult.getAllErrors());

            model.addAttribute("errorMessage", "회원가입 정보를 확인해주세요.");
            model.addAttribute("userRegisterRquestDto", requestDto);
            return "register";
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