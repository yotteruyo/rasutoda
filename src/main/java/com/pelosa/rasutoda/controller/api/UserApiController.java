package com.pelosa.rasutoda.controller.api;

import com.pelosa.rasutoda.dto.UserRegisterRequestDto;
import com.pelosa.rasutoda.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute UserRegisterRequestDto requestDto,
                               RedirectAttributes redirectAttributes) {
        try {
            userService.register(requestDto);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/register";
        }
        redirectAttributes.addFlashAttribute("successMessage", "회원가입 완료입니다.");
        return "redirect:/login";

    }
}