package com.example.zookeeperusersnodes.utils;

import java.util.regex.Pattern;

public class IPAddressChecker {
    private static final String IP_ADDRESS_PATTERN =
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?):\\d{1,5}$";

    // Compile the regular expression into a pattern
    private static final Pattern pattern = Pattern.compile(IP_ADDRESS_PATTERN);

    public static boolean isIPAddress(String input) {
        return pattern.matcher(input).matches();
    }
}
