package com.careerverse.util;

/** Escapes text before it is printed inside an HTML page (stops XSS through URL parameters). */
public class Html {
    public static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
