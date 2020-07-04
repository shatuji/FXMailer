package com.houarizegai.fxmailer.util;

import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;
import java.util.Scanner;

public class Tools {

    public static String loadTemplateFile(String filename) {
        try {
            /***
             * for windows path
             */
            File file ;
            if(checkOS().indexOf("Window") < 0)
                 file = new File(String.format("%s/%s.txt", Constants.HTML_TEMPLATE_LOCATION_MAC, filename, ".txt"));
            else
                 file = new File(String.format("%s\\%s.txt", Constants.HTML_TEMPLATE_LOCATION_WINDOWS, filename, ".txt"));
            StringBuilder fileContents = new StringBuilder((int) file.length());

            try (Scanner scanner = new Scanner(file)) {
                while (scanner.hasNextLine()) {
                    fileContents.append(scanner.nextLine() + System.lineSeparator());
                }
                return fileContents.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String replaceString(String input, String... replace) {
        for(int i = 0; i < replace.length; i++)
            input = input.replaceFirst("%s", replace[i]);

        return input;
    }

    /***
     * get system OS version to demaion what are you staying computer
     * @return
     */
    public static String checkOS(){
        return SystemUtils.OS_NAME;
    }

    /***
     * get properties by mail name
     * outgoing mail server detail
     */
    public static Properties getProperties(String emailName){
        Properties  props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        if(emailName.indexOf("gmail") > -1)
        {
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
        }
        if(emailName.indexOf("sina") > -1)
        {
            props.put("mail.smtp.host", "smtp.sina.com");
            props.put("mail.smtp.port", "25");
        }
        return props;
    }
}
