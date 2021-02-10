package com.samchenjava.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.samchenjava.common.utils.PageUtils;
import com.samchenjava.mall.product.entity.SpuInfoDescEntity;
import com.samchenjava.mall.product.entity.SpuInfoEntity;
import com.samchenjava.mall.product.vo.SpuSaveVo;

import java.util.Map;

/**
 * spu信息
 *
 * @author samchen
 * @email nan.chen.java@gmail.com
 * @date 2020-12-29 17:49:08
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSpuInfo(SpuSaveVo vo);//n.87

    void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity);//n.88

    PageUtils queryPageByCondition(Map<String, Object> params);//n.93

    //n.130 shelf product
    void shelfProduct(Long spuId);

    SpuInfoEntity getSpuInfoBySkuId(Long skuId);//n.278
}

