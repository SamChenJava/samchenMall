package com.samchenjava.mall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.samchenjava.common.utils.PageUtils;
import com.samchenjava.mall.coupon.entity.HomeSubjectSpuEntity;

import java.util.Map;

/**
 * 专题商品
 *
 * @author samchen
 * @email nan.chen.java@gmail.com
 * @date 2020-12-30 13:29:15
 */
public interface HomeSubjectSpuService extends IService<HomeSubjectSpuEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

