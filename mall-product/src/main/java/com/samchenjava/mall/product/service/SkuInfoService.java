package com.samchenjava.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.samchenjava.common.utils.PageUtils;
import com.samchenjava.mall.product.entity.SkuInfoEntity;
import com.samchenjava.mall.product.vo.SkuItemVo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * sku信息
 *
 * @author samchen
 * @email nan.chen.java@gmail.com
 * @date 2020-12-29 17:49:09
 */
public interface SkuInfoService extends IService<SkuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSkuInfo(SkuInfoEntity skuInfoEntity);//n.89

    PageUtils queryPageByCondition(Map<String, Object> params);//n.94

    List<SkuInfoEntity> getSkusBySpuId(Long spuId);//n.130

    SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException;//n.204
}

