package com.kmkm.clientdome.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    
    @Bean
    public WebClient webClient(OAuth2AuthorizedClientManager authorizedClientManager){
        // create WebClient to call secured APIs
        // gets a token from the authorizedClientManager, and adds it as a Bearer token.
        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2Client = new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);

        return WebClient.builder()
                        .apply(oauth2Client.oauth2Configuration())
                        .build();
    }
}
