package com.jmunoz.footballui.controllers;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

@Controller
public class FootballController {

    @GetMapping("/")
    public String home() {
        return "home";
    }

    // Mostramos la información del usuario.
    // Inyectamos OidcUser y creamos un modelo que usaremos en la view `myself`.
    @GetMapping("/myself")
    public String user(Model model, @AuthenticationPrincipal OidcUser oidcUser) {
        model.addAttribute("userName", oidcUser.getName());
        model.addAttribute("audience", oidcUser.getAudience());
        model.addAttribute("expiresAt", oidcUser.getExpiresAt());
        model.addAttribute("claims", oidcUser.getClaims());

        return "myself";
    }

    // Obtenemos la información de los equipos.
    @GetMapping("/teams")
    public String teams(@RegisteredOAuth2AuthorizedClient("football-ui")
                            OAuth2AuthorizedClient authorizedClient, Model model) {
        RestTemplate restTemplate = new RestTemplate();

        // Pasamos el token de acceso en la Authorization header, con el prefijo Bearer
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + authorizedClient.getAccessToken().getTokenValue());
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:8080/football/teams",
                HttpMethod.GET, entity, String.class);

        model.addAttribute("teams", response.getBody());

        return "teams";
    }
}
