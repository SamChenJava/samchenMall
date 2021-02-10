package com.samchenjava.mall.promotion.service;

import com.samchenjava.mall.promotion.to.SecKillSkuRedisTo;

import java.util.List;

public interface SeckillService {
    void shelfSecKillSkusInNext3Days();

    List<SecKillSkuRedisTo> getCurrentSeckillSkus();//n.318

    SecKillSkuRedisTo getSkuSeckillInfo(Long skuId);//n.319

    String kill(String killId, String key, Integer num);//n.322
}
