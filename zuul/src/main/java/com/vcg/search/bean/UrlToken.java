package com.vcg.search.bean;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dongsijia on 2019/2/14.
 */
public class UrlToken {
    private static Map<String, String> urlTokens = new HashMap<>();

    public static String get(String url) {
        return urlTokens.get(url);
    }

    public static void put(String url, String token) {
        urlTokens.put(url, token);
    }

    public static Map<String, String> getUrlTokens() {
        return urlTokens;
    }

    public static void setUrlTokens(Map<String, String> urlTokens) {
        UrlToken.urlTokens = urlTokens;
    }
}
