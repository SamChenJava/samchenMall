package com.samchenjava.mall.order.feign;

import com.samchenjava.common.utils.R;
import com.samchenjava.mall.order.vo.WareSkuLockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("mall-warehouse")
public interface WmsFeignService {

    @PostMapping("ware/waresku/hasStock")//n.132
    R getSkusHasStock(@RequestBody List<Long> skuIds);

    @GetMapping("ware/wareinfo/fare")//n.277
    R getFare(@RequestParam("addrId")Long addrId);

    @PostMapping("ware/waresku/lock/order")
    R orderLockStock(@RequestBody WareSkuLockVo vo);

}
