package com.chavez.qpan.util.support.matcher;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Mobile phone number check
 * 手机号码校验
 */
public class PhoneMatcherSupport {
    public static boolean isChinaPhoneLegal(String str) throws PatternSyntaxException {
        String regExp = "^((19[0-9])|(13[0-9])|(15[^4])|(18[0,2,3,5-9])|(17[0-8])|(147))\\d{8}$";
        Pattern p = Pattern.compile(regExp);
        Matcher m = p.matcher(str);
        return m.matches();
    }

    public static boolean isHKPhoneLegal(String str)throws PatternSyntaxException {
        String regExp = "^(5|6|8|9)\\d{7}$";
        Pattern p = Pattern.compile(regExp);
        Matcher m = p.matcher(str);
        return m.matches();
    }

    public static boolean isPhoneLegal(String str)throws PatternSyntaxException {
        return str.length() == 11 && isChinaPhoneLegal(str) || str.length() ==8 && isHKPhoneLegal(str);
    }

}
