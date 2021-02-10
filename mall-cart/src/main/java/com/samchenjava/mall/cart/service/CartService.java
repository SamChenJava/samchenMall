package com.samchenjava.mall.cart.service;

import com.samchenjava.mall.cart.vo.CartItemVo;
import com.samchenjava.mall.cart.vo.CartVo;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface CartService {
    CartItemVo addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;//n.241

    CartItemVo getCartItem(Long skuId);//n.243

    CartVo getCart() throws ExecutionException, InterruptedException;//n.244

    void clearCart(String cartKey);

    void checkItem(Long skuId, Integer check);//n.245

    void updateQuantity(Long skuId, Integer quantity);//n.246

    void deleteItem(Long skuId);//n.257 delete one of selected cart item

    List<CartItemVo> getCurrentUserCartItems();//n.266
}
