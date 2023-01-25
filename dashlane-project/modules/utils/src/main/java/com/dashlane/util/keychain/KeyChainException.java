package com.dashlane.util.keychain;



public class KeyChainException extends Exception {
    public KeyChainException(Exception e) {
        super(e);
    }
    public KeyChainException(String message) {
        super(message);
    }
}
