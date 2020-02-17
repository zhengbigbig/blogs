package hello.utils;

import java.util.regex.Pattern;

public class ValidateUtils {
    private static final Pattern USERNAME_STANDARD = Pattern.compile("^(?!_)(?!.*?_$)[a-zA-Z0-9_\\u4e00-\\u9fa5]{2,15}$");
    private static final Pattern PASSWORD_STANDARD = Pattern.compile("^[A-Za-z0-9.\\-_]{6,16}$");

    public static boolean username(String username){
        return USERNAME_STANDARD.matcher(username).find();
    }

    public static boolean password(String password){
        return PASSWORD_STANDARD.matcher(password).find();
    }
}
