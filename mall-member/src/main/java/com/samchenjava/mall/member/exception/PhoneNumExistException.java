package com.samchenjava.mall.member.exception;

public class PhoneNumExistException extends RuntimeException {
    public PhoneNumExistException(){
        super("phone number has already been registered");
    }
}
