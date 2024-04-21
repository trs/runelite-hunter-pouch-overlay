package com.pouch;

import java.util.regex.Pattern;

public class HunterPouchMessage {

    public static final Pattern POUCH_FULL = Pattern.compile("^Your pouch is full.$");

    public static final Pattern POUCH_EMPTY = Pattern.compile("^Your (?:meat|fur) pouch is empty.$");

    public static final Pattern POUCH_HOLDING = Pattern.compile("^Your (?:meat|fur) pouch is currently holding (\\d+) (?:meat|fur).$");
    public static boolean matches(Pattern pattern, String message) {
        return pattern.matcher(message).matches();
    }
}


