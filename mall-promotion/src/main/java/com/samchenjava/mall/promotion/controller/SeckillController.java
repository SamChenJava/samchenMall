package com.samchenjava.mall.promotion.controller;


import com.samchenjava.common.utils.R;
import com.samchenjava.mall.promotion.service.SeckillService;
import com.samchenjava.mall.promotion.to.SecKillSkuRedisTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class SeckillController {

    @Autowired
    SeckillService seckillService;

    //n.318 get skus which are eligible to envolve current second kill promotion
    @ResponseBody
    @GetMapping("/currentSeckillSkus")
    public R getCurrentSeckillSkus(){
        List<SecKillSkuRedisTo> vos = seckillService.getCurrentSeckillSkus();
        return R.ok().setData(vos);
    }

    @ResponseBody
    @GetMapping("/sku/seckill/{skuId}")//n.319
    public R getSkuSeckillInfo(@PathVariable(value = "skuId")Long skuId){
        SecKillSkuRedisTo to = seckillService.getSkuSeckillInfo(skuId);
        return R.ok().setData(to);
    }

    @GetMapping("/kill")//n.322
    public String seckill(@RequestParam("killId") String killId,
                     @RequestParam("key") String key,
                     @RequestParam("num") Integer num,
                          Model model){
        String orderSn = seckillService.kill(killId,key,num);
        model.addAttribute("orderSn", orderSn);
        return "success";
    }
}
