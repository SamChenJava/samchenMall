package com.samchenjava.mall.warehouse.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.samchenjava.common.utils.R;
import com.samchenjava.mall.warehouse.feign.MemberFeignService;
import com.samchenjava.mall.warehouse.vo.FareVo;
import com.samchenjava.mall.warehouse.vo.MemberAddressVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.samchenjava.common.utils.PageUtils;
import com.samchenjava.common.utils.Query;

import com.samchenjava.mall.warehouse.dao.WareInfoDao;
import com.samchenjava.mall.warehouse.entity.WareInfoEntity;
import com.samchenjava.mall.warehouse.service.WareInfoService;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Autowired
    MemberFeignService memberFeignService;

    @Override//n.95
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareInfoEntity> wareInfoEntityQueryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (StringUtils.isNotEmpty(key)) {
            wareInfoEntityQueryWrapper.eq("id", key)
                    .or().like("name", key)
                    .or().like("address", key)
                    .or().eq("areacode", key);
        }
        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                wareInfoEntityQueryWrapper
        );

        return new PageUtils(page);
    }

    @Override//n.272
    public FareVo getFare(Long addrId) {
        FareVo fareVo = new FareVo();
        R addressInfo = memberFeignService.getAddressInfo(addrId);
        MemberAddressVo data = addressInfo.getData("memberReceiveAddress",new TypeReference<MemberAddressVo>() {
        });
        if(data!=null){
            //mock calculate delivery fee
            String phone = data.getPhone();
            //phone number:123456789
            String substring = phone.substring(phone.length() - 1, phone.length());
            BigDecimal bigDecimal = new BigDecimal(substring);
            fareVo.setAddressVo(data);
            fareVo.setFare(bigDecimal);
            return fareVo;
        }
        return null;
    }

}