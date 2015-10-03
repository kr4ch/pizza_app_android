package com.JA.bletemperature.misc;

import org.apache.commons.lang3.StringEscapeUtils;


public class StringHandler {
    final static public String TAG = "StringHandler";
    
    /**
     * print the given string and it's special characters if it has any
     * @param msg
     */
    public static String printWithNonPrintableChars(String msg){
        return StringEscapeUtils.escapeJava(msg);
    }
    
    /**
     * searches the given response string for a start and end string, if both are found
     * it returns what is between the start and end string
     * @param start the string to search first
     * @param end the string to search for last
     * @param response the string to search
     * @return whatever is between the start and end strings (the start and end characters are not included).
     * returns null if no start or end string is found
     */
    public static String stripStringValue(String start, String end, String response){
        int startPos = response.indexOf(start);
        int endPos = response.indexOf(end);
        
        if(startPos != -1 || endPos != -1){
            return response.substring(startPos+1, endPos);
        }
        return null;
    }
}
