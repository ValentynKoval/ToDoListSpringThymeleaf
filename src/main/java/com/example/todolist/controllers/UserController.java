package com.example.todolist.controllers;

import com.example.todolist.dto.AuthUserDto;
import com.example.todolist.dto.UserDto;
import com.example.todolist.models.User;
import com.example.todolist.services.JwtService;
import com.example.todolist.services.MailSenderService;
import com.example.todolist.services.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping()
public class UserController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final MailSenderService mailSenderService;

    @GetMapping("/")
    public String mainPage() {
        return "main";
    }

    @GetMapping("/user/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("userDto", new UserDto());
        return "register";
    }

    @PostMapping("/user/register")
    public String registerUser(@ModelAttribute("userDto") @Valid UserDto userDto,
                               BindingResult result,
                               Model model) {
        if (result.hasErrors()) {
            return "register";
        }

        if (!userDto.passwordsMatch()) {
            model.addAttribute("passwordError", "Passwords do not match");
            return "register";
        }

        if (userDto.getPassword().length() < 8) {
            model.addAttribute("passwordError", "Password must be at least 8 characters long.");
            return "register";
        }

        if (userService.emailExists(userDto.getEmail())) {
            model.addAttribute("emailError", "Email is already in use.");
            return "register";
        }

        try {
            User user = userService.createNewUser(userDto);
            String message = "Thank you for registering for our service! It will be very useful for you in planning your day and assigning tasks.";
            mailSenderService.send(user.getEmail(), "Registration", message);
            return "redirect:/user/login?success";
        } catch (IllegalArgumentException e) {
            model.addAttribute("registrationError", e.getMessage());
            return "register";
        } catch (Exception e) {
            model.addAttribute("registrationError", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/user/login")
    public String showLoginForm(Model model) {
        model.addAttribute("authUserDto", new AuthUserDto());
        return "login";
    }

    @PostMapping("/user/login")
    public String loginUser(@Valid @ModelAttribute("authUserDto") AuthUserDto authUserDto,
                            BindingResult result,
                            Model model,
                            HttpServletResponse response) {
        if (result.hasErrors()) {
            return "login";
        }
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authUserDto.getUsername(), authUserDto.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = userService.getUserByEmail(authUserDto.getUsername());

            String token = jwtService.getTokens(user);

            Cookie refreshCookie = new Cookie("token", token);
            refreshCookie.setHttpOnly(true);
            refreshCookie.setSecure(true);
            refreshCookie.setPath("/");
            refreshCookie.setMaxAge(7 * 24 * 60 * 60);
            response.addCookie(refreshCookie);

            return "redirect:/tasks";
        } catch (Exception e) {
            model.addAttribute("loginError", "Invalid username or password");
            return "login";
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@CookieValue(value = "refresh_token", required = false)  String refreshToken) {
        try {
            if (refreshToken == null) {
                throw new Exception("Refresh token is null");
            }
            String accessToken = jwtService.refreshToken(refreshToken);
            return ResponseEntity.ok(new HashMap<>() {{put("access_token", accessToken);}});
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @GetMapping("/user/logout")
    public String logout(@CookieValue(value = "token", required = false) String refreshToken,
                         HttpServletResponse response) {
        SecurityContextHolder.clearContext();
        if (refreshToken != null) {
            jwtService.revokeToken(refreshToken);
            Cookie cookie = new Cookie("token", null);
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            cookie.setMaxAge(0);
            cookie.setPath("/");
            response.addCookie(cookie);
        }
        return "redirect:/user/login?logout";
    }

    @GetMapping("/info")
    public String getUserInfo(Model model) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println(email);
        model.addAttribute("user", userService.getUserDtoByEmail(email));
        return "user-info";
    }
}
