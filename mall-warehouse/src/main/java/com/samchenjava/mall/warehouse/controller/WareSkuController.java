package com.samchenjava.mall.warehouse.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.samchenjava.common.exception.BizCodeEnum;
import com.samchenjava.common.to.SkuHasStockTo;
import com.samchenjava.mall.warehouse.exception.NoStockException;
import com.samchenjava.mall.warehouse.vo.LockStockResultVo;
import com.samchenjava.mall.warehouse.vo.WareSkuLockVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.samchenjava.mall.warehouse.entity.WareSkuEntity;
import com.samchenjava.mall.warehouse.service.WareSkuService;
import com.samchenjava.common.utils.PageUtils;
import com.samchenjava.common.utils.R;


/**
 * 商品库存
 *
 * @author samchen
 * @email nan.chen.java@gmail.com
 * @date 2020-12-30 14:17:11
 */
@RestController
@RequestMapping("ware/waresku")
public class WareSkuController {
    @Autowired
    private WareSkuService wareSkuService;

    //n.96 sku info in certain warehouse
    @RequestMapping("/list")
    //@RequiresPermissions("warehouse:waresku:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = wareSkuService.queryPage(params);

        return R.ok().put("page", page);
    }


    @RequestMapping("/info/{id}")
    //@RequiresPermissions("warehouse:waresku:info")
    public R info(@PathVariable("id") Long id) {
        WareSkuEntity wareSku = wareSkuService.getById(id);

        return R.ok().put("wareSku", wareSku);
    }

    @RequestMapping("/save")
    //@RequiresPermissions("warehouse:waresku:save")
    public R save(@RequestBody WareSkuEntity wareSku) {
        wareSkuService.save(wareSku);

        return R.ok();
    }

    @RequestMapping("/update")
    //@RequiresPermissions("warehouse:waresku:update")
    public R update(@RequestBody WareSkuEntity wareSku) {
        wareSkuService.updateById(wareSku);

        return R.ok();
    }

    @RequestMapping("/delete")
    //@RequiresPermissions("warehouse:waresku:delete")
    public R delete(@RequestBody Long[] ids) {
        wareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

    //check whether these skus has stock
    @PostMapping("/hasStock")//n.132
    public R getSkusHasStock(@RequestBody List<Long> skuIds) {
        List<SkuHasStockTo> tos = wareSkuService.getSkuHasStock(skuIds);
        return R.ok().setData(tos);
    }

    @PostMapping("/lock/order")//n.281
    public R orderLockStock(@RequestBody WareSkuLockVo vo){
        try{
            Boolean stock = wareSkuService.orderLockStock(vo);
            return R.ok();
        }catch(NoStockException e){
            return R.error(BizCodeEnum.NO_STOCK_EXCEPTION.getCode(), BizCodeEnum.NO_STOCK_EXCEPTION.getMessage());
        }
    }
}
