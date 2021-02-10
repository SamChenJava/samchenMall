package com.samchenjava.mall.order.web;

import com.alipay.api.AlipayApiException;
import com.samchenjava.mall.order.config.AlipayTemplate;
import com.samchenjava.mall.order.service.OrderService;
import com.samchenjava.mall.order.vo.PayVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PayWebController {

    @Autowired
    AlipayTemplate alipayTemplate;

    @Autowired
    OrderService orderService;

    @GetMapping(value = "/payOrder", produces = "text/html")//n.304
    @ResponseBody
    public String payOrder(@RequestParam("orderSn")String orderSn) throws AlipayApiException {
        PayVo payVo = orderService.getOrderPay(orderSn);
        String pay = alipayTemplate.pay(payVo);
        return pay;
    }
}
