package com.samchenjava.mall.cart.controller;

import com.samchenjava.common.constant.AuthServerConstant;
import com.samchenjava.mall.cart.interceptor.CartInterceptor;
import com.samchenjava.mall.cart.service.CartService;
import com.samchenjava.mall.cart.vo.CartItemVo;
import com.samchenjava.mall.cart.vo.CartVo;
import com.samchenjava.mall.cart.vo.UserInfoTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Controller
public class CartController {

    @Autowired
    CartService cartService;

    @GetMapping("/cart.html")//n.244
    public String cartListPage(Model model) throws ExecutionException, InterruptedException {
        CartVo cartVo = cartService.getCart();
        model.addAttribute("cart", cartVo);
        return "cartList";
    }

    @GetMapping("/addToCart")//n.241 243
    public String addToCart(@RequestParam(value = "skuId", required = true) Long skuId,
                            @RequestParam(value = "num", required = true) Integer num,
                            RedirectAttributes redirectAttributes) throws ExecutionException, InterruptedException {
        cartService.addToCart(skuId, num);
        redirectAttributes.addAttribute("skuId", skuId);
        return "redirect:http://cart.gulimall.com/addToCartSuccess.html";
    }

    @GetMapping("/addToCartSuccess.html")//n.243
    public String addToCartSuccessPage(@RequestParam("skuId") Long skuId, Model model) {
        CartItemVo cartItemVo = cartService.getCartItem(skuId);
        model.addAttribute("item", cartItemVo);
        return "success";
    }

    @GetMapping("checkItem")//n.245
    public String checkItem(@RequestParam("skuId") Long skuId, @RequestParam("check") Integer check) {
        cartService.checkItem(skuId, check);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    @GetMapping("updateQuantity")//n.246
    public String updateQuantity(@RequestParam("skuId") Long skuId, @RequestParam("quantity") Integer quantity) {
        cartService.updateQuantity(skuId, quantity);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    @GetMapping("deleteItem")//n.247
    public String deleteItem(@RequestParam("skuId") Long skuId) {
        cartService.deleteItem(skuId);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    @GetMapping("/currentUserCartItems")//n.266
    @ResponseBody
    public List<CartItemVo> getCurrentUserCartItems(){
        return cartService.getCurrentUserCartItems();
    }

}
