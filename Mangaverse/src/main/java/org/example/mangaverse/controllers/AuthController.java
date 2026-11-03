package org.example.mangaverse.controllers;

import java.nio.charset.StandardCharsets;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.example.mangaverse.accounts.AccountService;
import org.example.mangaverse.accounts.SignupForm;
import org.example.mangaverse.library.LibraryStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final AccountService accounts;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository contextRepository;

    public AuthController(AccountService accounts, AuthenticationManager authenticationManager,
                          SecurityContextRepository contextRepository) {
        this.accounts = accounts;
        this.authenticationManager = authenticationManager;
        this.contextRepository = contextRepository;
    }

    @GetMapping("/login")
    public String login(Authentication authentication) {
        return isSignedIn(authentication) ? "redirect:/library" : "login";
    }

    @GetMapping("/signup")
    public String signupForm(Authentication authentication, Model model) {
        if (isSignedIn(authentication)) {
            return "redirect:/library";
        }
        model.addAttribute("form", new SignupForm());
        return "signup";
    }

    /** Creates the account and signs the user in right away, so the next page can sync their device. */
    @PostMapping("/signup")
    public String signup(@Valid @ModelAttribute("form") SignupForm form, BindingResult errors,
                         HttpServletRequest request, HttpServletResponse response) {
        // BCrypt only uses the first 72 bytes; refuse longer passwords instead of silently truncating them.
        if (form.getPassword() != null && form.getPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
            errors.rejectValue("password", "tooLong", "Слишком длинный пароль");
        }
        if (errors.hasErrors()) {
            return "signup";
        }
        try {
            accounts.register(form.getName(), form.getEmail(), form.getPassword());
        } catch (AccountService.EmailTakenException e) {
            errors.rejectValue("email", "taken", "Этот email уже зарегистрирован — войдите");
            return "signup";
        }
        Authentication auth = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(form.getEmail(), form.getPassword()));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        contextRepository.saveContext(context, request, response);
        request.changeSessionId();
        return "redirect:/library?synced=1";
    }

    @GetMapping("/library")
    public String library(Model model) {
        model.addAttribute("nav", "library");
        model.addAttribute("statuses", LibraryStatus.values());
        return "library";
    }

    private static boolean isSignedIn(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }
}
