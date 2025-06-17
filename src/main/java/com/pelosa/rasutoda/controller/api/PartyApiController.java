package com.pelosa.rasutoda.controller.api;

import com.pelosa.rasutoda.domain.User;
import com.pelosa.rasutoda.service.PartyServices;
import com.pelosa.rasutoda.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/parties")
@RequiredArgsConstructor
public class PartyApiController {

    private final PartyServices partyService;
    private final UserService userService;


    @DeleteMapping("/{partyId}/disband")
    public ResponseEntity<Map<String, String>> disbandParty(@PathVariable Long partyId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "로그인 상태가 아닙니다."));
            }
            User currentUser;
            Object principal = authentication.getPrincipal();
            if (principal instanceof org.springframework.security.core.userdetails.User) {
                String username = ((org.springframework.security.core.userdetails.User) principal).getUsername();
                currentUser = userService.findUserByLoginId(username)
                        .orElseThrow(() -> new IllegalStateException("로그인된 사용자(일반)를 찾을 수 없습니다: " + username));
            } else if (principal instanceof org.springframework.security.oauth2.core.user.OAuth2User) {
                org.springframework.security.oauth2.core.user.OAuth2User oauth2User = (org.springframework.security.oauth2.core.user.OAuth2User) principal;
                String providerId = oauth2User.getName();
                String provider = ((org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
                currentUser = userService.findByProviderAndProviderId(provider, providerId)
                        .orElseThrow(() -> new IllegalStateException("OAuth2 사용자(소셜)를 찾을 수 없습니다: " + providerId));
            } else {
                throw new IllegalStateException("알 수 없는 인증 주체 타입입니다. 사용자 정보를 가져올 수 없습니다.");
            }

            partyService.disbandParty(partyId, currentUser);
            return ResponseEntity.ok(Map.of("message", "파티가 성공적으로 해체되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            System.err.println("파티 해체 중 오류: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "파티 해체 중 서버 오류가 발생했습니다."));
        }
    }

    @DeleteMapping("/{partyId}/leave")
    public ResponseEntity<Map<String, String>> leaveParty(@PathVariable Long partyId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "로그인 상태가 아닙니다."));
            }
            User currentUser;
            Object principal = authentication.getPrincipal();
            if (principal instanceof org.springframework.security.core.userdetails.User) {
                String username = ((org.springframework.security.core.userdetails.User) principal).getUsername();
                currentUser = userService.findUserByLoginId(username)
                        .orElseThrow(() -> new IllegalStateException("로그인된 사용자(일반)를 찾을 수 없습니다: " + username));
            } else if (principal instanceof org.springframework.security.oauth2.core.user.OAuth2User) {
                org.springframework.security.oauth2.core.user.OAuth2User oauth2User = (org.springframework.security.oauth2.core.user.OAuth2User) principal;
                String providerId = oauth2User.getName();
                String provider = ((org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
                currentUser = userService.findByProviderAndProviderId(provider, providerId)
                        .orElseThrow(() -> new IllegalStateException("OAuth2 사용자(소셜)를 찾을 수 없습니다: " + providerId));
            } else {
                throw new IllegalStateException("알 수 없는 인증 주체 타입입니다. 사용자 정보를 가져올 수 없습니다.");
            }

            partyService.leaveParty(partyId, currentUser);
            return ResponseEntity.ok(Map.of("message", "파티에서 성공적으로 탈퇴했습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            System.err.println("파티 탈퇴 중 오류: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "파티 탈퇴 중 서버 오류가 발생했습니다."));
        }
    }

}