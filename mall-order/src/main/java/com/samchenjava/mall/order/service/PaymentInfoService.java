package com.samchenjava.mall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.samchenjava.common.utils.PageUtils;
import com.samchenjava.mall.order.entity.PaymentInfoEntity;

import java.util.Map;

/**
 * 支付信息表
 *
 * @author samchen
 * @email nan.chen.java@gmail.com
 * @date 2020-12-30 14:10:54
 */
public interface PaymentInfoService extends IService<PaymentInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

