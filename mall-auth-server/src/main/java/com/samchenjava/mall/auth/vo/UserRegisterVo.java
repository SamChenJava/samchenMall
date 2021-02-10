package com.samchenjava.mall.auth.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class UserRegisterVo {
    @NotNull(message = "username must be submit")
    @Length(min = 6, max=18, message = "username length between 6 and 18")
    private String username;
    @NotNull(message = "password must not be empty")
    @Length(min = 6, max=18, message = "password length between 6 and 18")
    private String password;
    @NotEmpty(message = "phone numer must not be empty")
    private String phone;
    @NotEmpty(message = "verification code must not be empty")
    private String code;
}
