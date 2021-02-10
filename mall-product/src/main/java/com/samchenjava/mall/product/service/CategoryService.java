package com.samchenjava.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.samchenjava.common.utils.PageUtils;
import com.samchenjava.mall.product.entity.CategoryEntity;
import com.samchenjava.mall.product.vo.Catelog2Vo;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author samchen
 * @email nan.chen.java@gmail.com
 * @date 2020-12-29 17:49:09
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> listWithTree();

    void removeMenuByIds(List<Long> asList);//v50

    //find full path of catelogId
    Long[] findCatalogPath(Long catelogId);//n.74

    void updateCascade(CategoryEntity category);//n.75

    List<CategoryEntity> getLevel1categories();

    Map<String, List<Catelog2Vo>> getCatalogJson();//n.138
}

