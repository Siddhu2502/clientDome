package com.kmkm.clientdome.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {
    @GetMapping("/")
    public String home(HttpSession session, Model model){
        
        String name = (String) session.getAttribute("USER_NAME");
        String loginId = (String) session.getAttribute("USER_LOGIN_ID");
        String phoneNumber = (String) session.getAttribute("USER_PHONE_NUMBER");

        model.addAttribute("userName", name);
        model.addAttribute("userNumber", phoneNumber);
        model.addAttribute("userLoginId", loginId);

        return "index";
    }
}