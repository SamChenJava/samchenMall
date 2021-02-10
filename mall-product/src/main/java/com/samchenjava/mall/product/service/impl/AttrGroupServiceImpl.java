package com.samchenjava.mall.product.service.impl;

import com.samchenjava.common.utils.Query;
import com.samchenjava.mall.product.entity.AttrEntity;
import com.samchenjava.mall.product.service.AttrService;
import com.samchenjava.mall.product.vo.AttrGroupWithAttrsVo;
import com.samchenjava.mall.product.vo.SpuItemAttrGroupVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.samchenjava.common.utils.PageUtils;

import com.samchenjava.mall.product.dao.AttrGroupDao;
import com.samchenjava.mall.product.entity.AttrGroupEntity;
import com.samchenjava.mall.product.service.AttrGroupService;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override//n.72, n.76 fixed
    public PageUtils queryPage(Map<String, Object> params, Long catalogId) {
        String key = (String) params.get("key");
        //select * from pms_attr_group where catalog_id = ? and (attr_group_id = key or attr_group_name like %key%
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<AttrGroupEntity>();
        if (StringUtils.isNotEmpty(key)) {
            wrapper.and((obj) -> {
                obj.eq("attr_group_id", key).or().like(" attr_group_name", key);
            });
        }
        if (catalogId == 0) {
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), wrapper);
            return new PageUtils(page);
        } else {
            wrapper.eq("catelog_id", catalogId);
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), wrapper);
            return new PageUtils(page);
        }
    }

    //n.85 get attr groups include attr under current category
    @Override
    public List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCategoryId(Long categoryId) {
        //get attr groups under current category
        List<AttrGroupEntity> groupEntities = this.baseMapper.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", categoryId));
        //get attrs in each attr groups
        List<AttrGroupWithAttrsVo> collect = groupEntities.stream().map(attrGroupEntity -> {
            AttrGroupWithAttrsVo attrGroupWithAttrsVo = new AttrGroupWithAttrsVo();
            BeanUtils.copyProperties(attrGroupEntity, attrGroupWithAttrsVo);
            List<AttrEntity> attrs = attrService.getRelationAttr(attrGroupEntity.getAttrGroupId());
            attrGroupWithAttrsVo.setAttrs(attrs);
            return attrGroupWithAttrsVo;
        }).collect(Collectors.toList());
        return collect;
    }

    @Override//n.205
    public List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(Long spuId, Long catalogId) {
        AttrGroupDao attrGroupDao = this.baseMapper;
        List<SpuItemAttrGroupVo> groupVos = attrGroupDao.getAttrGroupWithAttrsBySpuId(spuId, catalogId);
        return groupVos;
    }

}