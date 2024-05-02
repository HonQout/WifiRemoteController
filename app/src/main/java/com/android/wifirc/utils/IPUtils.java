package com.android.wifirc.utils;

public class IPUtils {
    public static boolean isValidIPAddress(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (!((c >= '0' && c <= '9') || c == '.')) {
                return false;
            }
        }
        String[] strings = s.split("\\.");
        if (strings.length != 4) {
            return false;
        }
        for (int i = 0; i < 4; i++) {
            int oneDigit = Integer.parseInt(strings[i]);
            if (!(oneDigit >= 0 && oneDigit <= 255 && String.valueOf(oneDigit).length() == strings[i].length())) {
                return false;
            }
        }
        return true;
    }

    public static boolean isValidPortNumber(int i) {
        return i >= 0 && i <= 65535;
    }

    public static boolean isValidPortNumber(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (!((c >= '0' && c <= '9'))) {
                return false;
            }
        }
        int port = Integer.parseInt(s);
        return isValidPortNumber(port);
    }
}
