package fi.jubic.quanta.auth;

import fi.jubic.easyconfig.annotations.EasyConfigProperty;

public class Admin {
    private final String username;
    private final String password;

    public Admin(
            @EasyConfigProperty(
                    value = "USERNAME",
                    defaultValue = "admin"
            ) String username,
            @EasyConfigProperty(
                    value = "PASSWORD",
                    defaultValue = "test"
            ) String password
    ) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

}


