package com.careerverse.util;

import com.careerverse.config.DB;

import jakarta.servlet.http.HttpServletRequest;

/** Small helpers to describe a request in security emails. */
public class RequestInfo {

    /** Real visitor IP. On Render the app sits behind a proxy that sets X-Forwarded-For. */
    public static String ip(HttpServletRequest r) {
        String fwd = r.getHeader("X-Forwarded-For");
        if (fwd != null && !fwd.isBlank()) return fwd.split(",")[0].trim();
        return r.getRemoteAddr();
    }

    /** "Chrome on Windows", "Safari on iPhone" ... from the User-Agent header. */
    public static String device(HttpServletRequest r) {
        String ua = r.getHeader("User-Agent");
        if (ua == null) return "Unknown device";
        String browser = ua.contains("Edg/") ? "Edge"
                : ua.contains("OPR/") || ua.contains("Opera") ? "Opera"
                : ua.contains("Chrome/") ? "Chrome"
                : ua.contains("Firefox/") ? "Firefox"
                : ua.contains("Safari/") ? "Safari" : "Browser";
        String os = ua.contains("Windows") ? "Windows"
                : ua.contains("Android") ? "Android"
                : ua.contains("iPhone") ? "iPhone"
                : ua.contains("iPad") ? "iPad"
                : ua.contains("Mac OS") ? "macOS"
                : ua.contains("Linux") ? "Linux" : "unknown OS";
        return browser + " on " + os;
    }

    /** Public website address: APP_URL if set, otherwise worked out from the request. */
    public static String appUrl(HttpServletRequest r) {
        String configured = DB.prop("app.url");
        if (!configured.isEmpty()) return configured.replaceAll("/+$", "");
        String proto = r.getHeader("X-Forwarded-Proto");
        String scheme = proto != null && !proto.isBlank() ? proto : r.getScheme();
        String host = r.getHeader("Host");
        if (host == null || host.isBlank()) host = r.getServerName() + ":" + r.getServerPort();
        return scheme + "://" + host + r.getContextPath();
    }
}
