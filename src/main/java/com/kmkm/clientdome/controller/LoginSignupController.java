package com.kmkm.clientdome.controller;


import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.descope.exception.DescopeException;
import com.descope.model.jwt.Token;
import com.descope.sdk.auth.AuthenticationService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class LoginSignupController {

    // log all the info (level DEBUG)
    private static final Logger logger = LoggerFactory.getLogger(LoginSignupController.class);
    // instance of authentication service porvided by descope
    private final AuthenticationService authenticationService;

    public LoginSignupController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @GetMapping("/loginsignup")
    public String loginPage(){
        return "loginsignup";
    }

    // validate the login api endpoint thing
    @PostMapping("/api/validate-session")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> validateSession(@RequestBody Map<String, String> payload, HttpServletRequest request){
        // check for session token
        String sessionToken = payload.get("token");
        if (sessionToken == null || sessionToken.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Token is missing"));
        }

        try {

            Token validatedToken = authenticationService.validateSessionWithToken(sessionToken);
            // get phone number
            Map<String, Object> claims = validatedToken.getClaims();
            String loginId = (String) claims.get("sub");
            String name = (String) claims.get("name");
            String phone = (String) claims.get("phone");

            if (name == null || name.isBlank()){
                name = "User name not returned";
            }

            logger.info("Validated the user with the loginId: {}", loginId);

            // spring secrutiy 
            List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(loginId, null, authorities);

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            HttpSession session = request.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", context);
            session.setAttribute("USER_NAME", name);
            session.setAttribute("USER_PHONE_NUMBER", phone);
            session.setAttribute("USER_LOGIN_ID", loginId);

            logger.info("Spring security context created for the user {}", loginId);

            return ResponseEntity.ok(Map.of("redirectUrl", "/"));


        
        } catch (DescopeException e) {  
            logger.error("Descope Authentication validation failed", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid token"));
        }
    }


}
