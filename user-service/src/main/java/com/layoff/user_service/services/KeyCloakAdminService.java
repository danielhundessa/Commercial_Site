package com.layoff.user_service.services;

import com.layoff.user_service.dtos.UserRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class KeyCloakAdminService {

    @Value("${keycloak.admin.username}")
    private String adminUsername;

    @Value("${keycloak.admin.password}")
    private String adminPassword;

    @Value("${keycloak.admin.server-url}")
    private String keycloakServerUrl;

    @Value("${keycloak.admin.realm}")
    private String realm;

    @Value("${keycloak.admin.client-id}")
    private String clientId;

    private final RestTemplate restTemplate = new RestTemplate();

    public String getAdminAccessToken() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", clientId);
        params.add("username", adminUsername);
        params.add("password", adminPassword);
        params.add("grant_type", "password");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> entity =
                new HttpEntity<>(params, headers);

        String url = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    url,
                    entity,
                    Map.class
            );

            if (response.getBody() == null || !response.getBody().containsKey("access_token")) {
                throw new RuntimeException("Failed to obtain access token from Keycloak. Response: " + response.getBody());
            }

            return (String) response.getBody().get("access_token");
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            throw new RuntimeException("Failed to authenticate with Keycloak. Status: " + e.getStatusCode() + 
                    ", Response: " + e.getResponseBodyAsString(), e);
        }
    }

    public String createUser(String token, UserRequest userRequest) {
        if (token == null || token.isEmpty()) {
            throw new RuntimeException("Invalid access token provided for user creation");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        Map<String, Object> userPayload = new HashMap<>();
        userPayload.put("username", userRequest.getUsername());
        userPayload.put("email", userRequest.getEmail());
        userPayload.put("enabled", true);
        userPayload.put("firstName", userRequest.getFirstName());
        userPayload.put("lastName", userRequest.getLastName());

        Map<String, Object> credential = new HashMap<>();
        credential.put("type", "password");
        credential.put("value", userRequest.getPassword());
        credential.put("temporary", false);

        userPayload.put("credentials", List.of(credential));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(userPayload, headers);

        String url = keycloakServerUrl + "/admin/realms/" + realm + "/users";

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    url,
                    entity,
                    String.class
            );

            if (!HttpStatus.CREATED.equals(response.getStatusCode())) {
                throw new RuntimeException("Failed to create user in keycloak. Status: " + 
                        response.getStatusCode() + ", Response: " + response.getBody());
            }

            // Extract Keycloak user id
            URI location = response.getHeaders().getLocation();
            if (location == null) {
                throw new RuntimeException("Keycloak did not return Location Header. Response: " + response.getBody());
            }

            String path = location.getPath();
            return path.substring(path.lastIndexOf("/") + 1);
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                throw new RuntimeException("Access forbidden. The user '" + adminUsername + 
                        "' may not have admin privileges or the client '" + clientId + 
                        "' may not have the required permissions. Response: " + e.getResponseBodyAsString(), e);
            }
            throw new RuntimeException("Failed to create user in Keycloak. Status: " + e.getStatusCode() + 
                    ", Response: " + e.getResponseBodyAsString(), e);
        }
    }

    private Map<String, Object> getRealmRoleRepresentation(String token,
                                                           String roleName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        String url = keycloakServerUrl + "/admin/realms/" +
                realm + "/roles/" + roleName;
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            return response.getBody();
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new RuntimeException("Realm role '" + roleName + "' not found in realm '" + realm + 
                        "'. Please create the role in Keycloak first.", e);
            }
            throw new RuntimeException("Failed to get realm role '" + roleName + "'. Status: " + 
                    e.getStatusCode() + ", Response: " + e.getResponseBodyAsString(), e);
        }
    }

    public void assignRealmRoleToUser(String username, String roleName, String userId) {
        String token = getAdminAccessToken();
        Map<String, Object> roleRep = getRealmRoleRepresentation(
                token, roleName
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<List<Map<String, Object>>> entity =
                new HttpEntity<>(List.of(roleRep), headers);

        String url = keycloakServerUrl + "/admin/realms/" +
                realm + "/users/" + userId + "/role-mappings/realm";

        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(
                    url, entity, Void.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException(
                        "Failed to assign realm role " + roleName +
                                " to user " + username +
                                ": HTTP " + response.getStatusCode()
                );
            }
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            throw new RuntimeException("Failed to assign realm role '" + roleName + 
                    "' to user '" + username + "'. Status: " + e.getStatusCode() + 
                    ", Response: " + e.getResponseBodyAsString(), e);
        }
    }
}