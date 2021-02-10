package com.samchenjava.mall.coupon.service.impl;

import com.samchenjava.common.to.MemberPrice;
import com.samchenjava.common.to.SkuReductionTo;
import com.samchenjava.mall.coupon.entity.MemberPriceEntity;
import com.samchenjava.mall.coupon.entity.SkuLadderEntity;
import com.samchenjava.mall.coupon.service.MemberPriceService;
import com.samchenjava.mall.coupon.service.SkuLadderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.samchenjava.common.utils.PageUtils;
import com.samchenjava.common.utils.Query;

import com.samchenjava.mall.coupon.dao.SkuFullReductionDao;
import com.samchenjava.mall.coupon.entity.SkuFullReductionEntity;
import com.samchenjava.mall.coupon.service.SkuFullReductionService;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {

    @Autowired
    SkuLadderService skuLadderService;

    @Autowired
    MemberPriceService memberPriceService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    @Override//.90
    public void saveSkuReduction(SkuReductionTo to) {
        //save $xx off every $100 info (sms_sku_ladder), member price
        //sku promotion info :  mall_sms -> sms_sku_ladder,
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        skuLadderEntity.setSkuId(to.getSkuId());
        skuLadderEntity.setFullCount(to.getFullCount());
        skuLadderEntity.setDiscount(to.getDiscount());
        skuLadderEntity.setAddOther(to.getCountStatus());//whether discount relevant to any other promotion
        if (to.getFullCount() > 0) {
            skuLadderService.save(skuLadderEntity);
        }
        //sms_sku_full_reduction,
        SkuFullReductionEntity reductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(to, reductionEntity);
        if (reductionEntity.getFullPrice().compareTo(new BigDecimal("0")) == 1) {
            this.save(reductionEntity);
        }
        //sms_member_price
        List<MemberPrice> memberPrices = to.getMemberPrice();
        List<MemberPriceEntity> collect = memberPrices.stream().map(item -> {
            MemberPriceEntity memberPriceEntity = new MemberPriceEntity();
            memberPriceEntity.setSkuId(to.getSkuId());
            memberPriceEntity.setMemberLevelId(item.getId());
            memberPriceEntity.setMemberLevelName(item.getName());
            memberPriceEntity.setMemberPrice(item.getPrice());
            memberPriceEntity.setAddOther(1);
            return memberPriceEntity;
        }).filter(item -> {
            return item.getMemberPrice().compareTo(new BigDecimal("0")) == 1;
        }).collect(Collectors.toList());
        memberPriceService.saveBatch(collect);
    }

}