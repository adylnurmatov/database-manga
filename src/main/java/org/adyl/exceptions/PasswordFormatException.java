package org.adyl.exceptions;

public class PasswordFormatException extends Exception{
    private String message;
    public PasswordFormatException() {
        super("Password format is incorrect!");
        this.message = "Password format is incorrect!\n\"Minimal one uppercase and lowercase English letter, + minimal one digit and one special character!\"";
    }

    public PasswordFormatException(String message) {
        super(message);
        this.message = message;
    }
}
