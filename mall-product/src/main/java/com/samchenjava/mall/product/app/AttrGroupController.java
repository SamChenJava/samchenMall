package com.samchenjava.mall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.samchenjava.mall.product.entity.AttrEntity;
import com.samchenjava.mall.product.service.AttrAttrgroupRelationService;
import com.samchenjava.mall.product.service.AttrService;
import com.samchenjava.mall.product.service.CategoryService;
import com.samchenjava.mall.product.vo.AttrGroupRelationVo;
import com.samchenjava.mall.product.vo.AttrGroupWithAttrsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.samchenjava.mall.product.entity.AttrGroupEntity;
import com.samchenjava.mall.product.service.AttrGroupService;
import com.samchenjava.common.utils.PageUtils;
import com.samchenjava.common.utils.R;


/**
 * 属性分组
 *
 * @author samchen
 * @email nan.chen.java@gmail.com
 * @date 2020-12-29 17:49:09
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private AttrAttrgroupRelationService attrAttrgroupRelationService;

    /**
     * n.72
     * get attribute group by catalogId
     */
    @RequestMapping("/list/{catalogId}")
    //@RequiresPermissions("product:attrgroup:list")
    public R list(@RequestParam Map<String, Object> params, @PathVariable("catalogId") Long catalogId) {
        PageUtils page = attrGroupService.queryPage(params, catalogId);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    //@RequiresPermissions("product:attrgroup:info")
    public R info(@PathVariable("attrGroupId") Long attrGroupId) {
        AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        //get full path of catalog, for display catalog during update base attribute group
        Long catalogId = attrGroup.getCatelogId();
        Long[] path = categoryService.findCatalogPath(catalogId);

        attrGroup.setCatelogPath(path);
        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds) {
        attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

    //n.80 product/attrgroup/{attrgroupId/attr/relation}
    @GetMapping("/{attrgroupId}/attr/relation")
    public R attrRelation(@PathVariable("attrgroupId") Long attrgroupId) {
        List<AttrEntity> entities = attrService.getRelationAttr(attrgroupId);
        return R.ok().put("data", entities);
    }

    //n.80 product/attrgroup/attr/relation/delete
    @PostMapping("/attr/relation/delete")
    public R deleteRelation(@RequestBody AttrGroupRelationVo[] vos) {
        attrService.deleteRelation(vos);
        return R.ok();
    }

    //n.81 /product/attrgroup/{attrgroupId}/noattr/relation
    @GetMapping("{attrgroupId}/noattr/relation")
    public R attrNoRelation(@PathVariable("attrgroupId") Long attrgroupId
            , @RequestParam Map<String, Object> params) {
        PageUtils page = attrService.getNoRelationAttr(params, attrgroupId);
        return R.ok().put("page", page);
    }

    //n.82 /product/attrgroup/attr/relation
    @PostMapping("/attr/relation")
    public R addRelation(@RequestBody List<AttrGroupRelationVo> vos) {
        attrAttrgroupRelationService.saveBatch(vos);
        return R.ok();
    }

    //n.85 /product/attrgroup/{catelogId}/withattr
    @GetMapping("/{catelogId}/withattr")
    public R getAttrGroupsWithAttrs(@PathVariable("catelogId") Long categoryId) {
        //get attr groups under current category
        List<AttrGroupWithAttrsVo> vos = attrGroupService.getAttrGroupWithAttrsByCategoryId(categoryId);
        return R.ok().put("data", vos);
    }

}
