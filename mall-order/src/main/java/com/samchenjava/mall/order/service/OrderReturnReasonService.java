package com.samchenjava.mall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.samchenjava.common.utils.PageUtils;
import com.samchenjava.mall.order.entity.OrderReturnReasonEntity;

import java.util.Map;

/**
 * 退货原因
 *
 * @author samchen
 * @email nan.chen.java@gmail.com
 * @date 2020-12-30 14:10:54
 */
public interface OrderReturnReasonService extends IService<OrderReturnReasonEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

