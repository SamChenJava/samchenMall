package com.samchenjava.mall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.samchenjava.common.constant.CartConstant;
import com.samchenjava.common.utils.R;
import com.samchenjava.mall.cart.feign.ProductFeignService;
import com.samchenjava.mall.cart.interceptor.CartInterceptor;
import com.samchenjava.mall.cart.service.CartService;
import com.samchenjava.mall.cart.vo.CartItemVo;
import com.samchenjava.mall.cart.vo.CartVo;
import com.samchenjava.mall.cart.vo.SkuInfoVo;
import com.samchenjava.mall.cart.vo.UserInfoTo;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Slf4j
@Service
public class cartServiceImpl implements CartService {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    ThreadPoolExecutor threadPoolExecutor;

    private final String CART_PREFIX = "gulimall:cart:";

    @Override//n.241
    public CartItemVo addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String jsonCart = (String) cartOps.get(skuId.toString());

        if (StringUtils.isEmpty(jsonCart)) {
            CartItemVo cartItemVo = new CartItemVo();
            //current shopping cart does not contain current sku
            CompletableFuture<Void> getSkuInfo = CompletableFuture.runAsync(() -> {
                R r = productFeignService.getSkuInfo(skuId);
                SkuInfoVo skuInfo = r.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                });
                cartItemVo.setCheck(true);
                cartItemVo.setCount(num);
                cartItemVo.setImage(skuInfo.getSkuDefaultImg());
                cartItemVo.setTitle(skuInfo.getSkuTitle());
                cartItemVo.setSkuId(skuId);
                cartItemVo.setPrice(skuInfo.getPrice());
            }, threadPoolExecutor);

            CompletableFuture<Void> getSkuAttrValues = CompletableFuture.runAsync(() -> {
                List<String> attrValues = productFeignService.getSkuSaleAttrValues(skuId);
                cartItemVo.setSkuAttr(attrValues);
            }, threadPoolExecutor);
            CompletableFuture.allOf(getSkuInfo, getSkuAttrValues).get();
            String jsonString = JSON.toJSONString(cartItemVo);
            cartOps.put(skuId.toString(), jsonString);
            return cartItemVo;
        } else {
            //current shopping cart contains current sku, update quantity
            CartItemVo cartItemVo1 = JSON.parseObject(jsonCart, CartItemVo.class);
            cartItemVo1.setCount(cartItemVo1.getCount() + num);
            cartOps.put(skuId.toString(), JSON.toJSONString(cartItemVo1));
            return cartItemVo1;
        }

    }

    @Override//n.243
    public CartItemVo getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String jsonItem = (String) cartOps.get(skuId.toString());
        CartItemVo cartItemVo = JSON.parseObject(jsonItem, CartItemVo.class);
        return cartItemVo;
    }

    @Override//n.244
    public CartVo getCart() throws ExecutionException, InterruptedException {
        CartVo cartVo = new CartVo();
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();

        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        if (userInfoTo.getUserId() != null) {
            //logged in
            //check whether temporary shopping cart is empty
            List<CartItemVo> tempShoppingCart = getCartItems(CART_PREFIX + userInfoTo.getUserKey());
            if (tempShoppingCart != null) {
                //combine logged in shopping cart and temporary shopping cart
                for (CartItemVo cartItemVo : tempShoppingCart) {
                    addToCart(cartItemVo.getSkuId(), cartItemVo.getCount());
                }
                //clear temporary shopping cart
                clearCart(CART_PREFIX + userInfoTo.getUserKey());
            }
            //get logged in shopping cart
            String cartKey = CART_PREFIX + userInfoTo.getUserId();
            List<CartItemVo> loginShoppingCartItems = getCartItems(cartKey);
            cartVo.setItems(loginShoppingCartItems);
            return cartVo;
        } else {
            //not logged in
            String cartKey = CART_PREFIX + userInfoTo.getUserKey();
            //get items in temporary shopping cart
            List<CartItemVo> cartItems = getCartItems(cartKey);
            cartVo.setItems(cartItems);
        }
        return cartVo;
    }

    @Override
    public void clearCart(String cartKey) {
        stringRedisTemplate.delete(cartKey);
    }

    @Override//n.245
    public void checkItem(Long skuId, Integer check) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItemVo cartItem = getCartItem(skuId);
        cartItem.setCheck(check == 1);
        String jsonString = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(), jsonString);
    }

    @Override//n.246
    public void updateQuantity(Long skuId, Integer quantity) {
        CartItemVo cartItem = getCartItem(skuId);
        cartItem.setCount(quantity);
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));
    }

    @Override//n.247
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.delete(skuId.toString());
    }

    @Override//n.266
    public List<CartItemVo> getCurrentUserCartItems() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
       if(userInfoTo.getUserId()==null){
           //not logged in
           return null;
       }else{
           String cartKey = CART_PREFIX + userInfoTo.getUserId();
           List<CartItemVo> cartItems = getCartItems(cartKey);
           //get selected items in shopping cart
           List<CartItemVo> collect = cartItems.stream()
                   .filter(CartItemVo::getCheck)
                   .map(item ->{
                       //update to latest price
                       item.setPrice(productFeignService.getPrice(item.getSkuId()));
                       return item;
                   }).collect(Collectors.toList());
           return collect;
       }
    }

    //n.244
    public List<CartItemVo> getCartItems(String cartKey) {
        BoundHashOperations<String, Object, Object> hashOps = stringRedisTemplate.boundHashOps(cartKey);
        List<Object> values = hashOps.values();
        if (values != null && values.size() > 0) {
            List<CartItemVo> collect = values.stream().map((obj) -> {
                String objStr = (String) obj;
                CartItemVo cartItemVo = JSON.parseObject(objStr, CartItemVo.class);
                return cartItemVo;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }

    //get the shopping cart
    private BoundHashOperations<String, Object, Object> getCartOps() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        String cartKey = "";
        //logged in
        if (userInfoTo.getUserId() != null) {
            //gulimall:cart:1
            cartKey = CART_PREFIX + userInfoTo.getUserId();
        } else {
            //gulimall:cart:xxxx-xxxx-xxxx-xxxx
            cartKey = CART_PREFIX + userInfoTo.getUserKey();
        }
        BoundHashOperations<String, Object, Object> hashOps = stringRedisTemplate.boundHashOps(cartKey);
        return hashOps;
    }
}
