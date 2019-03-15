package fi.jubic.quanta.exception;

public class InputException extends ApplicationException {
    public InputException() {
        super();
    }

    public InputException(String s) {
        super(s);
    }

    public InputException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public InputException(Throwable throwable) {
        super(throwable);
    }

    public InputException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
