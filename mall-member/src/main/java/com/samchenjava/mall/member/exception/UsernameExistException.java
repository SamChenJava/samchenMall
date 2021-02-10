package com.samchenjava.mall.member.exception;

public class UsernameExistException extends RuntimeException {
    public UsernameExistException(){
        super("username has already been registered");
    }
}
