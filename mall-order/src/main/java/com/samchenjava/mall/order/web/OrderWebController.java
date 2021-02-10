package com.samchenjava.mall.order.web;

import com.samchenjava.mall.order.service.OrderService;
import com.samchenjava.mall.order.vo.OrderConfirmVo;
import com.samchenjava.mall.order.vo.OrderSubmitVo;
import com.samchenjava.mall.order.vo.SubmitOrderRespVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.concurrent.ExecutionException;

@Controller
public class OrderWebController {

    @Autowired
    OrderService orderService;

    @GetMapping("toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {
        OrderConfirmVo orderConfirmVo = orderService.confirmOrder();
        model.addAttribute("orderConfirmData", orderConfirmVo);
        return "confirm";
    }

    @PostMapping("submitOrder")//n.275
    public String submitOrder(OrderSubmitVo orderSubmitVo, Model model, RedirectAttributes redirectAttributes){
        SubmitOrderRespVo orderRespVo = orderService.submitOrder(orderSubmitVo);
        if(orderRespVo.getCode()==0){
            //submitting order succeed and redirect to payment page
            model.addAttribute("submitOrderResp",orderRespVo);
            return "pay";
        }else{
            String msg = "submit order failed.";//n.282
            switch (orderRespVo.getCode()){
                case 1: msg += "token verification failed."; break;
                case 2: msg += "payAmount verification failed."; break;
                case 3: msg += "locking stock failed."; break;
            }
            redirectAttributes.addFlashAttribute("msg", msg);
            return "redirect:http://order.gulimall.com/toTrade";
        }
    }
}
