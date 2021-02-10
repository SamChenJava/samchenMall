package com.samchenjava.mall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.samchenjava.common.valid.AddGroup;
import com.samchenjava.common.valid.UpdateGroup;
import com.samchenjava.common.valid.UpdateStatusGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.samchenjava.mall.product.entity.BrandEntity;
import com.samchenjava.mall.product.service.BrandService;
import com.samchenjava.common.utils.PageUtils;
import com.samchenjava.common.utils.R;

/**
 * 品牌
 *
 * @author samchen
 * @email nan.chen.java@gmail.com
 * @date 2020-12-29 17:49:09
 */
@RestController
@RequestMapping("product/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * list
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:brand:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * info
     */
    @RequestMapping("/info/{brandId}")
    //@RequiresPermissions("product:brand:info")
    public R info(@PathVariable("brandId") Long brandId) {
        BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    @GetMapping("/infos")//n.192
    public R info(@RequestParam("brandIds") List<Long> brandIds) {
        List<BrandEntity> brandEntities = brandService.getBrandsByIds(brandIds);
        return R.ok().put("brand", brandEntities);
    }

    /**
     * no 67,68
     * save a brand
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:brand:save")
    public R save(@Validated({AddGroup.class}) @RequestBody BrandEntity brand/*, BindingResult bindingResult*/) {
/*        if (bindingResult.hasErrors()) {
            Map<String, String> map = new HashMap<>();
            bindingResult.getFieldErrors().forEach((item) -> {
                //type of item is FieldError
                String message = item.getDefaultMessage();
                String field = item.getField();
                map.put(field, message);
            });
            return R.error(400, "submitted data error").put("data",map);
        } else {*/
        brandService.save(brand);
        return R.ok();
    }

    @RequestMapping("/update")
    //@RequiresPermissions("product:brand:update")
    public R update(@Validated(UpdateGroup.class) @RequestBody BrandEntity brand) {
        //brandService.updateById(brand);
        brandService.updateDetail(brand);
        return R.ok();
    }

    /**
     * n.69
     * update status
     */
    @RequestMapping("/update/status")
    //@RequiresPermissions("product:brand:update")
    public R updateStatus(@Validated(UpdateStatusGroup.class) @RequestBody BrandEntity brand) {
        brandService.updateById(brand);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:brand:delete")
    public R delete(@RequestBody Long[] brandIds) {
        brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}
