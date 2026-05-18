package ru.servicecenter.client.session;

import ru.servicecenter.client.dto.AuthResponse;

public final class Session {

    private static AuthResponse currentUser;

    private Session() {
    }

    public static void setUser(AuthResponse user) {
        currentUser = user;
    }

    public static AuthResponse getUser() {
        return currentUser;
    }

    public static String getToken() {
        return currentUser != null ? currentUser.getToken() : null;
    }

    public static Long getUserId() {
        return currentUser != null ? currentUser.getUserId() : null;
    }

    public static String getRole() {
        return currentUser != null ? currentUser.getRole() : null;
    }

    public static boolean isAdmin() {
        return "ADMIN".equals(getRole());
    }

    public static boolean isManager() {
        return "MANAGER".equals(getRole());
    }

    public static boolean isMaster() {
        return "MASTER".equals(getRole());
    }

    public static void clear() {
        currentUser = null;
    }
}
