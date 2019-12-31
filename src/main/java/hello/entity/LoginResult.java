package hello.entity;

public class LoginResult extends Result<User> {
    private boolean isLogin;


    public LoginResult(String status, String msg, User user, boolean isLogin) {
        super(status, msg, user);
        this.isLogin = isLogin;
    }

    public static LoginResult failure(String msg) {
        return new LoginResult("fail", msg, null, false);
    }

    public static LoginResult success(String msg, User user) {
        if (user == null) {
            return new LoginResult("ok", msg, null, true);
        } else {
            return new LoginResult("ok", msg, user, true);
        }
    }

    public boolean isLogin() {
        return isLogin;
    }
}
