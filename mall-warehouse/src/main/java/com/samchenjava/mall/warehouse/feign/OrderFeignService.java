package com.samchenjava.mall.warehouse.feign;

import com.samchenjava.common.utils.R;
import com.samchenjava.mall.warehouse.entity.WareOrderTaskEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("mall-order")
public interface OrderFeignService {

    @GetMapping("order/order/status/{orderSn}")//n.295
    R getOrderStatus(@PathVariable("orderSn") String orderSn);

}
