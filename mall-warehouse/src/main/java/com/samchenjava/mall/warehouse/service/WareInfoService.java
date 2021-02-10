package com.samchenjava.mall.warehouse.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.samchenjava.common.utils.PageUtils;
import com.samchenjava.mall.warehouse.entity.WareInfoEntity;
import com.samchenjava.mall.warehouse.vo.FareVo;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 仓库信息
 *
 * @author samchen
 * @email nan.chen.java@gmail.com
 * @date 2020-12-30 14:17:11
 */
public interface WareInfoService extends IService<WareInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    FareVo getFare(Long addrId);//n.272
}

