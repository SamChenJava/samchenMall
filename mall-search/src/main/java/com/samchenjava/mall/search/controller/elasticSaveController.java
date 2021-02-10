package com.samchenjava.mall.search.controller;

import com.samchenjava.common.exception.BizCodeEnum;
import com.samchenjava.common.to.es.SkuEsModel;
import com.samchenjava.common.utils.R;
import com.samchenjava.mall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RequestMapping("/search/save")
@RestController//n.133
public class elasticSaveController {

    @Autowired
    ProductSaveService productSaveService;

    //shelf product
    @PostMapping("/product")//n.133
    public R productStatusShelf(@RequestBody List<SkuEsModel> skuEsModels) {
        boolean b = false;
        try {
            b = productSaveService.productStatusOnShelf(skuEsModels);
        } catch (Exception e) {
            log.error("elasticSaveController put on shelf error: {}", e);
            return R.error(BizCodeEnum.PRODUCT_SHELF_EXCEPTION.getCode(), BizCodeEnum.PRODUCT_SHELF_EXCEPTION.getMessage());
        }
        if (!b) {
            return R.ok();
        } else {
            return R.error(BizCodeEnum.PRODUCT_SHELF_EXCEPTION.getCode(), BizCodeEnum.PRODUCT_SHELF_EXCEPTION.getMessage());
        }
    }
}
