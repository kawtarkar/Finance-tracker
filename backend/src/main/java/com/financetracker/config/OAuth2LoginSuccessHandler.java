package com.financetracker.config;


import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.financetracker.model.User;
import com.financetracker.repository.UserRepository;
import com.financetracker.model.AuthProvider;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;


@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtUtil jwtService; 

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                         HttpServletResponse response,
                                         Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        User user = userRepository.findByEmail(email)
            .orElseGet(() -> {
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setFirstName(name);
                newUser.setProvider(AuthProvider.GOOGLE);
                newUser.setEnabled (true);
                
                
                return userRepository.save(newUser);
            });

        String token = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        String redirectUrl = UriComponentsBuilder.fromUriString("http://localhost:5173/oauth2/redirect")
            .queryParam("token", token)
            .queryParam("refreshToken", refreshToken)
            .build().toUriString();

        response.sendRedirect(redirectUrl);
    }
}