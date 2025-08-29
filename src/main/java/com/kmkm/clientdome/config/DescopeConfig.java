package com.kmkm.clientdome.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.descope.client.Config;
import com.descope.client.DescopeClient;
import com.descope.exception.DescopeException;
import com.descope.sdk.auth.AuthenticationService;


@Configuration
public class DescopeConfig {
    
    @Value("${descope.project-id}")
    private String descopeProjectId;

    @Bean
    public DescopeClient descopeClient() throws DescopeException{
        return new DescopeClient(Config.builder().projectId(descopeProjectId).build());
    }

    @Bean
    public AuthenticationService authenticationService(DescopeClient descopeClient){
        return descopeClient.getAuthenticationServices().getAuthService();
    }
}
