package com.samchenjava.mall.promotion.feign;

import com.samchenjava.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient("mall-coupon")
public interface CouponFeignService {

    @GetMapping("coupon/seckillsession/next3DaysSessions")//n.313
    R getNext3DaysSessions();
}
