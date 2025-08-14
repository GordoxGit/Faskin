package com.heneria.skinview.util;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Utilitaires JSON légers via regex (réponses Mojang simples). */
public final class JsonUtils {
    private JsonUtils() {}

    private static String q(String s) { return Pattern.quote(s); }

    /** Cherche "key":"value" (niveau simple). */
    public static Optional<String> findString(String json, String key) {
        Matcher m = Pattern.compile("\\"" + q(key) + "\\"\\s*:\\s*\\"([^\\"]*)\\"").matcher(json);
        return m.find() ? Optional.ofNullable(m.group(1)) : Optional.empty();
    }

    /** Dans la réponse sessionserver: properties[..].value (Base64). */
    public static Optional<String> findFirstValuePropertyBase64(String json) {
        Matcher arr = Pattern.compile("\\"properties\\"\\s*:\\s*\\[(.*?)\\]", Pattern.DOTALL).matcher(json);
        if (!arr.find()) return Optional.empty();
        Matcher mv = Pattern.compile("\\"value\\"\\s*:\\s*\\"([^\\"]+)\\"").matcher(arr.group(1));
        return mv.find() ? Optional.ofNullable(mv.group(1)) : Optional.empty();
    }

    /** Dans la réponse sessionserver: properties[..].signature. */
    public static Optional<String> findFirstValuePropertySignature(String json) {
        Matcher arr = Pattern.compile("\\"properties\\"\\s*:\\s*\\[(.*?)\\]", Pattern.DOTALL).matcher(json);
        if (!arr.find()) return Optional.empty();
        Matcher ms = Pattern.compile("\\"signature\\"\\s*:\\s*\\"([^\\"]+)\\"").matcher(arr.group(1));
        return ms.find() ? Optional.ofNullable(ms.group(1)) : Optional.empty();
    }

    /** `textures` → SKIN.url */
    public static Optional<String> findSkinUrl(String texturesJson) {
        return findString(texturesJson, "url");
    }

    /** `textures` → SKIN.metadata.model (ex: "slim") */
    public static Optional<String> findSkinModel(String texturesJson) {
        return findString(texturesJson, "model");
    }
}

