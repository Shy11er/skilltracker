package net.brekker.skilltracker.auth.security;

import java.util.List;
import java.util.Map;

final class OAuth2Attributes {
    private OAuth2Attributes() {}

    static Normalized normalize(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> normalizeGoogle(attributes);
            case "yandex" -> normalizeYandex(attributes);
            default -> new Normalized(null, null, null);
        };
    }

    private static Normalized normalizeGoogle(Map<String, Object> attributes) {
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String id = (String) attributes.get("id");

        return new Normalized(email, name, id);
    }

    private static Normalized normalizeYandex(Map<String, Object> attributes) {
        String email = (String) attributes.get("default_email");
        if (email == null) {
            Object emails = attributes.get("emails");
            if (emails instanceof List<?> list && !list.isEmpty()) {
                Object first = list.getFirst();
                if (first != null) {
                    email = (String) first;
                }
            }
        }

        String username = (String) attributes.get("real_name");
        if (username == null) {
            String firstName = (String) attributes.get("first_name");
            String lastName = (String) attributes.get("last_name");

            if (firstName != null && lastName != null) {
                username = String.format("%s %s", firstName, lastName).trim();
            }
            if (username == null) {
                username = (String) attributes.get("login");
            }

        }
        String id = String.valueOf(attributes.get("id"));

        return new Normalized(email, username, id);
    }

    static record Normalized(String email, String name, String providerUserId) {}
}
