package com.samchenjava.mall.product.feign;

import com.samchenjava.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("mall-warehouse")
public interface WarehouseFeignService {

    @PostMapping("ware/waresku/hasStock")
//n.132
    R getSkusHasStock(@RequestBody List<Long> skuIds);
}
