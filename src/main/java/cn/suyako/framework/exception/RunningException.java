package cn.suyako.framework.exception;

public class RunningException extends Exception {
    public RunningException(String message) {
        super(message);
    }

    public RunningException(String message, Throwable err) {
        super(message, err);
    }
}
