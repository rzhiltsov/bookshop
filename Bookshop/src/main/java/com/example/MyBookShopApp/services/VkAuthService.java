package com.example.MyBookShopApp.services;

import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.json.JsonParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

@Service
public class VkAuthService {

    private final String vkAppId;
    private final String vkServiceKey;
    private final HttpServletRequest request;

    @Autowired
    public VkAuthService(@Value("${vk.app-id}") String vkAppId, @Value("${vk.service-key}") String vkServiceKey, HttpServletRequest request) {
        this.vkAppId = vkAppId;
        this.vkServiceKey = vkServiceKey;
        this.request = request;
    }

    public String buildVkAuthRef() {
        return "https://id.vk.com/auth" +
                "?uuid=" + UUID.randomUUID() +
                "&app_id=" + vkAppId +
                "&response_type=silent_token" +
                "&redirect_uri=https://" + request.getServerName() + "/vk_auth";
    }

    public Map<String, ?> getUserInfo(String token, String uuid) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://api.vk.com/method/auth.getProfileInfoBySilentToken" +
                "?v=5.199" +
                "&lang=0" +
                "&access_token=" + vkServiceKey +
                "&token=" + token +
                "&uuid=" + uuid;
        ResponseEntity<ObjectNode> response = restTemplate.getForEntity(url, ObjectNode.class);
        try {
            return new JacksonJsonParser().parseMap(response.getBody().get("response").get("success").get(0).toString());
        } catch (JsonParseException | NullPointerException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }
}
