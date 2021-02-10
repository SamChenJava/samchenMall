package com.samchenjava.mall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.samchenjava.common.utils.Query;
import com.samchenjava.common.utils.R;
import com.samchenjava.mall.product.config.MyThreadConfig;
import com.samchenjava.mall.product.entity.SkuImagesEntity;
import com.samchenjava.mall.product.entity.SpuInfoDescEntity;
import com.samchenjava.mall.product.feign.PromotionFeignService;
import com.samchenjava.mall.product.service.*;
import com.samchenjava.mall.product.vo.SeckillInfoVo;
import com.samchenjava.mall.product.vo.SkuItemSaleAttrVo;
import com.samchenjava.mall.product.vo.SkuItemVo;
import com.samchenjava.mall.product.vo.SpuItemAttrGroupVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.samchenjava.common.utils.PageUtils;

import com.samchenjava.mall.product.dao.SkuInfoDao;
import com.samchenjava.mall.product.entity.SkuInfoEntity;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    AttrGroupService attrGroupService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    ThreadPoolExecutor executor;

    @Autowired
    PromotionFeignService promotionFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override//n.89
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.baseMapper.insert(skuInfoEntity);
    }

    @Override//n.94
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (StringUtils.isNotEmpty(key)) {
            queryWrapper.and(q -> {
                q.eq("sku_id", key).or().like("sku_name", key);
            });
        }
        String catelogId = (String) params.get("catelogId");
        if (StringUtils.isNotEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
            queryWrapper.eq("catalog_id", catelogId);
        }
        String brandId = (String) params.get("brandId");
        if (StringUtils.isNotEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            queryWrapper.eq("brand_id", brandId);
        }
        String min = (String) params.get("min");
        if (StringUtils.isNotEmpty(min)) {
            queryWrapper.ge("price", min);
        }
        String max = (String) params.get("max");
        if (StringUtils.isNotEmpty(max)) {
            try {
                BigDecimal bigDecimal = new BigDecimal(max);
                if (bigDecimal.compareTo(new BigDecimal("0")) == 1) {
                    queryWrapper.le("price", max);
                }
            } catch (Exception e) {
            }
        }
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                queryWrapper
        );
        return new PageUtils(page);
    }

    @Override//n.130
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        List<SkuInfoEntity> spuInfoEntities = this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));
        return spuInfoEntities;
    }

    @Override//n.204
    public SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException {
        SkuItemVo skuItemVo = new SkuItemVo();
        CompletableFuture<SkuInfoEntity> skuInfoEntityCompletableFuture = CompletableFuture.supplyAsync(() -> {
            //n.205 get sku info, pms_sku_info
            SkuInfoEntity infoEntity = this.getById(skuId);
            skuItemVo.setSkuInfoEntity(infoEntity);
            return infoEntity;
        }, executor);

        CompletableFuture<Void> saleAttrFuture = skuInfoEntityCompletableFuture.thenAcceptAsync((res) -> {
            //n.206 sale attribute combination of spu which current sku belongs to
            List<SkuItemSaleAttrVo> skuItemSaleAttrVos = skuSaleAttrValueService.getSaleAttrBySpuId(res.getSpuId());
            skuItemVo.setSaleAttrs(skuItemSaleAttrVos);
        }, executor);

        CompletableFuture<Void> skuDescFuture = skuInfoEntityCompletableFuture.thenAcceptAsync((res) -> {
            //spu intro, pms_spu_info_desc
            SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getById(res.getSpuId());
            skuItemVo.setDesp(spuInfoDescEntity);
        }, executor);

        CompletableFuture<Void> baseAttrGroupFuture = skuInfoEntityCompletableFuture.thenAcceptAsync((res) -> {
            //n.205 spu base attribute
            List<SpuItemAttrGroupVo> spuItemAttrGroupVos = attrGroupService.getAttrGroupWithAttrsBySpuId(res.getSpuId(), res.getCatalogId());
            skuItemVo.setAttrGroups(spuItemAttrGroupVos);
        }, executor);

        CompletableFuture<Void> imageFuture = CompletableFuture.runAsync(() -> {
            //sku image info, pms_sku_images
            List<SkuImagesEntity> images = skuImagesService.getImagesBySkuId(skuId);
            skuItemVo.setSkuImages(images);
        }, executor);

        //n.319 whether current sku envolved in second kill promotion
        CompletableFuture<Void> seckillSkuInfoFuture = CompletableFuture.runAsync(() -> {
            R skuSeckillInfo = promotionFeignService.getSkuSeckillInfo(skuId);
            if (skuSeckillInfo.getCode() == 0) {
                SeckillInfoVo seckillInfoVo = skuSeckillInfo.getData(new TypeReference<SeckillInfoVo>() {
                });
                skuItemVo.setSeckillInfo(seckillInfoVo);
            }
        },executor);

        //wait until all task finish
        CompletableFuture.allOf(saleAttrFuture, skuDescFuture,
                baseAttrGroupFuture, imageFuture, seckillSkuInfoFuture).get();
        return skuItemVo;
    }

}