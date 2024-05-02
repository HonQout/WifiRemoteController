package com.android.wifirc.utils;

public class StringUtils {
    public static String appendForwardCharacter(int num, int bits, char c) {
        String str_num = String.valueOf(num);
        if (str_num.length() >= bits) {
            return str_num;
        } else {
            StringBuffer sb = new StringBuffer(str_num);
            sb.reverse();
            for (int i = 0; i < bits - str_num.length(); i++) {
                sb.append(c);
            }
            return sb.reverse().toString();
        }
    }
}