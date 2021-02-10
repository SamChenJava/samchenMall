package com.samchenjava.mall.member.feign;

import com.samchenjava.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@FeignClient("mall-order")
public interface OrderFeignService {

    @RequestMapping("order/order/listWithItem")//n.306
    //@RequiresPermissions("order:order:list")
    R listWithItem(@RequestBody Map<String, Object> params);
}
