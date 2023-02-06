package cn.suyako.framework.exception;

public class BuildException extends Exception {
    public BuildException(String message) {
        super(message);
    }

    public BuildException(String message, Throwable err) {
        super(message, err);
    }
}
