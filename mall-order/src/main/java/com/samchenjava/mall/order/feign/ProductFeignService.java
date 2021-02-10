package com.samchenjava.mall.order.feign;

import com.samchenjava.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("mall-product")
public interface ProductFeignService {

    @GetMapping("/product/spuinfo/skuId/{id}")//n.278
    R getSpuInfoBySkuId(@PathVariable("id") Long skuId);
}
