package fi.jubic.quanta.exception;

import javax.ws.rs.NotAuthorizedException;

public class AuthorizationException extends NotAuthorizedException {
    public AuthorizationException() {
        super("");
    }

    public AuthorizationException(String s) {
        super(s);
    }

    public AuthorizationException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public AuthorizationException(Throwable throwable) {
        super(throwable);
    }

    public AuthorizationException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
