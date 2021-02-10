package com.samchenjava.mall.product.feign;

import com.samchenjava.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("mall-promotion")
public interface PromotionFeignService {

    @GetMapping("/sku/seckill/{skuId}")//n.319
    R getSkuSeckillInfo(@PathVariable(value = "skuId")Long skuId);
}
