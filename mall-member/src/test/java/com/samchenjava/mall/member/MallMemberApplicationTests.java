package com.samchenjava.mall.member;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

//@SpringBootTest
//@RunWith(SpringRunner.class)
public class MallMemberApplicationTests {

    @Test
    public void contextLoads() {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        //$2a$10$gaGnLt2E1bFn0SjuiJ7MDOx4nbT0Z4GN8S3iwVodAdkrPDKan4Wui
        //$2a$10$4wFlCxvQNUQHNWYHI/NJMOzWoswcWI5FGddROkw1mQePYUvcceiqu
        String encode = bCryptPasswordEncoder.encode("123456");
        boolean b = bCryptPasswordEncoder.matches("123456", "$2a$10$4wFlCxvQNUQHNWYHI/NJMOzWoswcWI5FGddROkw1mQePYUvcceiqu");
        System.out.println(b);
    }

}
