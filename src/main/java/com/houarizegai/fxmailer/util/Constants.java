package com.houarizegai.fxmailer.util;

import java.nio.file.Paths;

public class Constants {
    public static final String HTML_TEMPLATE_LOCATION_MAC;
    public static final String HTML_TEMPLATE_LOCATION_WINDOWS;
    // get relative template folder path
    static {
        /***
         * for Mac path
         */
        HTML_TEMPLATE_LOCATION_MAC = new StringBuilder().append(Paths.get("").toAbsolutePath())
                .append("/src/main/resources/template").toString();
        /***
         * for Windows
         */
        HTML_TEMPLATE_LOCATION_WINDOWS = new StringBuilder().append(Paths.get("").toAbsolutePath())
                .append("\\src\\main\\resources\\template\\").toString();
    }
}
