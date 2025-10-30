package com.nhonguoiyeucu.openlinkedhub.utils;

import java.text.Normalizer;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;


public class NameUtils {
    // Map alias tối thiểu mẫu; có thể mở rộng từ cấu hình
    private static final Map<String, String> ALIAS_PREFIX;
    static {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("q. ", "");
        m.put("quan ", "");
        m.put("quận ", "");
        m.put("h. ", "");
        m.put("huyen ", "");
        m.put("huyện ", "");
        ALIAS_PREFIX = Collections.unmodifiableMap(m);
    }


    private static final Pattern DIACRITICS = Pattern.compile("\\p{M}+");

    public static String normalizeDistrictDisplay(String raw) {
        if (raw == null) return null;
        return raw.trim().replaceAll("\\s+", " ");
    }

    public static String stripAccents(String s) {
        if (s == null) return null;
        String nfd = Normalizer.normalize(s, Normalizer.Form.NFD);
        return DIACRITICS.matcher(nfd).replaceAll("");
    }

    public static String toNormalizedKey(String raw) {
        if (raw == null) return null;
        String s = normalizeDistrictDisplay(raw).toLowerCase(Locale.ROOT);
        for (var e : ALIAS_PREFIX.entrySet()) {
            if (s.startsWith(e.getKey())) {
                s = e.getValue() + s.substring(e.getKey().length());
            }
        }
        s = stripAccents(s);
        s = s.replaceAll("[^a-z0-9 ]", "").trim().replaceAll("\\s+", " ");
        return s;
    }
}

