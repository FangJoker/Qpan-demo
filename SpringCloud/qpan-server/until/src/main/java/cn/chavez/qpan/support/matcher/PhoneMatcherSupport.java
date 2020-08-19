package cn.chavez.qpan.support.matcher;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @Author: ChavezQiu
 * @description:
 * @Date: 2020/3/19 16:46
 */
public class PhoneMatcherSupport {
    public static boolean isChinaPhoneLegal(String str) throws PatternSyntaxException {
        String regExp = "^((13[0-9])|(15[^4])|(19[0-9])|(18[0,2,3,5-9])|(17[0-8])|(147))\\d{8}$";
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