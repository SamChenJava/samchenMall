package com.samchenjava.common.exception;

/**
 * no.67
 * definition class of error code and error class
 * 1 error code is defined as 5 digits
 * 2 first two digits stand for service type, last three stand for error code.
 * 3 error description need to be updated if error code updated. define them as enum type
 * 4 error code list:
 * 10 universal
 * 11 product
 * 12 order
 * 13 shopping cart
 * 14 logistic
 */
public enum BizCodeEnum {
    UNKNOW_EXCEPTION(10000, "unknow exception"),
    VALID_EXCEPTION(10001, "data validation error."),
    TOO_MANY_REQUEST(10002, "request amount overflow"),
    VALID_SMS_CODE_EXCEPTION(10002, "短信验证频率太高，请稍后再试"),
    PRODUCT_SHELF_EXCEPTION(11000, "product put on shelf error"),
    USER_EXIST_EXCEPTION(15001, "user exist error"),
    PHONE_EXIST_EXCEPTION(15002, "手机号存在异常"),
    NO_STOCK_EXCEPTION(21000, "product stock insufficient"),
    LOGINACCOUNT_PASSWORD_INVALID_EXCEPTION(15003, "account number or password invalid");

    private int code;
    private String message;

    BizCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
