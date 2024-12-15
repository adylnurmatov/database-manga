package org.adyl.exceptions;

public class ItemNotFoundException extends Exception {
    public ItemNotFoundException() {
        super("Such object was not found!");
    }

    public ItemNotFoundException(String message) {
        super(message);
    }
}
