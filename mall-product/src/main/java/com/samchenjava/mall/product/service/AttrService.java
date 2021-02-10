package com.samchenjava.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.samchenjava.common.utils.PageUtils;
import com.samchenjava.mall.product.entity.AttrEntity;
import com.samchenjava.mall.product.entity.ProductAttrValueEntity;
import com.samchenjava.mall.product.vo.AttrGroupRelationVo;
import com.samchenjava.mall.product.vo.AttrRespVo;
import com.samchenjava.mall.product.vo.AttrVo;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author samchen
 * @email nan.chen.java@gmail.com
 * @date 2020-12-29 17:49:08
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAttr(AttrVo attr);//n.76

    //n.77
    PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String attrType);

    //n.78
    AttrRespVo getAttrInfo(Long attrId);

    //n.78
    void updateAttr(AttrVo attr);

    List<AttrEntity> getRelationAttr(Long attrgroupId);//n.80

    void deleteRelation(AttrGroupRelationVo[] vos);//n.80

    PageUtils getNoRelationAttr(Map<String, Object> params, Long attrgroupId);//n.81

    List<Long> selectSearchableAttrIds(List<Long> attrIds);//n.131 get searchable base attrs in specific base attrs collection
}

