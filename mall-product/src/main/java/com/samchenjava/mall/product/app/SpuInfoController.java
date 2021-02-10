package com.samchenjava.mall.product.app;

import java.util.Arrays;
import java.util.Map;

import com.samchenjava.mall.product.vo.SpuSaveVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.samchenjava.mall.product.entity.SpuInfoEntity;
import com.samchenjava.mall.product.service.SpuInfoService;
import com.samchenjava.common.utils.PageUtils;
import com.samchenjava.common.utils.R;


/**
 * spu信息
 *
 * @author samchen
 * @email nan.chen.java@gmail.com
 * @date 2020-12-29 17:49:08
 */
@RestController
@RequestMapping("product/spuinfo")
public class SpuInfoController {
    @Autowired
    private SpuInfoService spuInfoService;

    /**
     * n.93
     * query spu
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:spuinfo:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = spuInfoService.queryPageByCondition(params);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("product:spuinfo:info")
    public R info(@PathVariable("id") Long id) {
        SpuInfoEntity spuInfo = spuInfoService.getById(id);

        return R.ok().put("spuInfo", spuInfo);
    }

    //n.87
    @RequestMapping("/save")
    //@RequiresPermissions("product:spuinfo:save")
    public R save(@RequestBody SpuSaveVo vo) {
        //spuInfoService.save(spuInfo);
        spuInfoService.saveSpuInfo(vo);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:spuinfo:update")
    public R update(@RequestBody SpuInfoEntity spuInfo) {
        spuInfoService.updateById(spuInfo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:spuinfo:delete")
    public R delete(@RequestBody Long[] ids) {
        spuInfoService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

    //n.130 /product/spuinfo/{spuId}/up
    @PostMapping("/{spuId}/up")
    public R spuShelf(@PathVariable("spuId") Long spuId) {
        spuInfoService.shelfProduct(spuId);
        return R.ok();
    }

    @GetMapping("/skuId/{id}")//n.278
    public R getSpuInfoBySkuId(@PathVariable("id") Long skuId){
        SpuInfoEntity spuInfoEntity = spuInfoService.getSpuInfoBySkuId(skuId);
        return R.ok().setData(spuInfoEntity);
    }
}
