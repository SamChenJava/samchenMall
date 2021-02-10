package com.samchenjava.mall.order.feign;

import com.samchenjava.mall.order.vo.MemberAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient("mall-member")
public interface MemberFeignService {

    @GetMapping("member/memberreceiveaddress/{memberId}/address")//n.266
    List<MemberAddressVo> getAddresses(@PathVariable(value = "memberId") Long memberId);
}
