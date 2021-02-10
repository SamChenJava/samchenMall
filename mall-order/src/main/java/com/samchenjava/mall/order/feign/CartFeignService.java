package com.samchenjava.mall.order.feign;

import com.samchenjava.mall.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient("mall-cart")
public interface CartFeignService {

    @GetMapping("/currentUserCartItems")//n.266
    List<OrderItemVo> getCurrentUserCartItems();
}
