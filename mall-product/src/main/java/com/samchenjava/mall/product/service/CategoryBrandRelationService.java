package com.samchenjava.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.samchenjava.common.utils.PageUtils;
import com.samchenjava.mall.product.entity.BrandEntity;
import com.samchenjava.mall.product.entity.CategoryBrandRelationEntity;

import java.util.List;
import java.util.Map;

/**
 * 品牌分类关联
 *
 * @author samchen
 * @email nan.chen.java@gmail.com
 * @date 2020-12-29 17:49:09
 */
public interface CategoryBrandRelationService extends IService<CategoryBrandRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveDetail(CategoryBrandRelationEntity categoryBrandRelation);//n.75

    //n.75
    void updateBrand(Long brandId, String name);

    //n.75
    void updateCategory(Long catId, String name);

    //n.84
    List<BrandEntity> getBrandsByCatId(Long catId);
}

