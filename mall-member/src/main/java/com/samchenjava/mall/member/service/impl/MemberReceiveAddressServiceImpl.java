package com.samchenjava.mall.member.service.impl;

import com.samchenjava.common.utils.R;
import com.samchenjava.mall.member.service.MemberReceiveAddressService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.samchenjava.common.utils.PageUtils;
import com.samchenjava.common.utils.Query;

import com.samchenjava.mall.member.dao.MemberReceiveAddressDao;
import com.samchenjava.mall.member.entity.MemberReceiveAddressEntity;


@Service("memberReceiveAddressService")
public class MemberReceiveAddressServiceImpl extends ServiceImpl<MemberReceiveAddressDao, MemberReceiveAddressEntity> implements MemberReceiveAddressService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberReceiveAddressEntity> page = this.page(
                new Query<MemberReceiveAddressEntity>().getPage(params),
                new QueryWrapper<MemberReceiveAddressEntity>()
        );

        return new PageUtils(page);
    }

    @Override//n.266
    public List<MemberReceiveAddressEntity> getAddresses(Long memberId) {
        MemberReceiveAddressDao dao = this.baseMapper;
        List<MemberReceiveAddressEntity> memberReceiveAddressEntities = dao.selectList(new QueryWrapper<MemberReceiveAddressEntity>().eq("member_id", memberId));
        return memberReceiveAddressEntities;
    }

}