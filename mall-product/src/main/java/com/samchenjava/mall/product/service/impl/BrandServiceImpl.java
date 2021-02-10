package com.samchenjava.mall.product.service.impl;

import com.samchenjava.common.utils.Query;
import com.samchenjava.mall.product.service.CategoryBrandRelationService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.samchenjava.common.utils.PageUtils;

import com.samchenjava.mall.product.dao.BrandDao;
import com.samchenjava.mall.product.entity.BrandEntity;
import com.samchenjava.mall.product.service.BrandService;
import org.springframework.transaction.annotation.Transactional;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Override//n.75
    public PageUtils queryPage(Map<String, Object> params) {
        //get keyword
        String key = (String) params.get("key");
        QueryWrapper<BrandEntity> brandEntityQueryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(key)) {
            brandEntityQueryWrapper.eq("brand_id", key).or().like("name", key);
        }
        IPage<BrandEntity> page = this.page(
                new Query<BrandEntity>().getPage(params),
                brandEntityQueryWrapper
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override//n.75
    public void updateDetail(BrandEntity brand) {
        //make sure redundancy filed consistency
        this.updateById(brand);
        if (StringUtils.isNotEmpty(brand.getName())) {
            //synchronize data of relevant table
            categoryBrandRelationService.updateBrand(brand.getBrandId(), brand.getName());
        }
    }

    @Override//n.192
    public List<BrandEntity> getBrandsByIds(List<Long> brandIds) {
        return baseMapper.selectList(new QueryWrapper<BrandEntity>().in("brand_id", brandIds));
    }

}