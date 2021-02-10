package com.samchenjava.mall.warehouse.service;

import com.samchenjava.mall.warehouse.vo.MergeVo;
import com.samchenjava.mall.warehouse.vo.PurchaseDoneVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.samchenjava.common.utils.PageUtils;
import com.samchenjava.mall.warehouse.entity.PurchaseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author sam chen
 * @email nan.chen.java@gmail.com
 * @date 2019-11-17 13:50:10
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageUnreceivePurchase(Map<String, Object> params);


    void mergePurchase(MergeVo mergeVo);


    void received(List<Long> ids);


    void done(PurchaseDoneVo doneVo);


}

