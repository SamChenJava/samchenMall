package com.samchenjava.mall.warehouse.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.samchenjava.common.to.SkuHasStockTo;
import com.samchenjava.common.to.mq.OrderTo;
import com.samchenjava.common.to.mq.StockLockedTo;
import com.samchenjava.common.utils.PageUtils;
import com.samchenjava.mall.warehouse.entity.WareSkuEntity;
import com.samchenjava.mall.warehouse.vo.LockStockResultVo;
import com.samchenjava.mall.warehouse.vo.WareSkuLockVo;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author samchen
 * @email nan.chen.java@gmail.com
 * @date 2020-12-30 14:17:11
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<SkuHasStockTo> getSkuHasStock(List<Long> skuIds);//n.132

    Boolean orderLockStock(WareSkuLockVo vo);//n.280

    void unlockStock(StockLockedTo to);//n.296

    void unlockStock(OrderTo orderTo);
}

