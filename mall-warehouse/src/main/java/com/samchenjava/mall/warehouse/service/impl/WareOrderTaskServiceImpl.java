package com.samchenjava.mall.warehouse.service.impl;

import org.springframework.stereotype.Service;

import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.samchenjava.common.utils.PageUtils;
import com.samchenjava.common.utils.Query;

import com.samchenjava.mall.warehouse.dao.WareOrderTaskDao;
import com.samchenjava.mall.warehouse.entity.WareOrderTaskEntity;
import com.samchenjava.mall.warehouse.service.WareOrderTaskService;


@Service("wareOrderTaskService")
public class WareOrderTaskServiceImpl extends ServiceImpl<WareOrderTaskDao, WareOrderTaskEntity> implements WareOrderTaskService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WareOrderTaskEntity> page = this.page(
                new Query<WareOrderTaskEntity>().getPage(params),
                new QueryWrapper<WareOrderTaskEntity>()
        );

        return new PageUtils(page);
    }

    @Override//n.298
    public WareOrderTaskEntity getOrderTaskByOrderSn(String orderSn) {
        WareOrderTaskEntity orderTaskEntity = this.getOne(new QueryWrapper<WareOrderTaskEntity>().eq("order_sn", orderSn));
        return orderTaskEntity;
    }

}