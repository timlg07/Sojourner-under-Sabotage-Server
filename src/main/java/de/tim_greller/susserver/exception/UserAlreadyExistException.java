package de.tim_greller.susserver.exception;

public class UserAlreadyExistException extends Exception {
    public UserAlreadyExistException(String s) {
        super(s);
    }
}
