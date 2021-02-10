package com.samchenjava.mall.product;

import com.samchenjava.mall.product.entity.BrandEntity;
import com.samchenjava.mall.product.service.AttrGroupService;
import com.samchenjava.mall.product.service.BrandService;
import com.samchenjava.mall.product.service.CategoryService;
import com.samchenjava.mall.product.service.SkuSaleAttrValueService;
import com.samchenjava.mall.product.vo.SkuItemSaleAttrVo;
import com.samchenjava.mall.product.vo.SpuItemAttrGroupVo;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.Redisson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class MallProductApplicationTests {

    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    Redisson redisson;

    @Autowired
    AttrGroupService attrGroupService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Test
    public void contextLoads() {
        List<BrandEntity> brandEntityList = brandService.list();
        for (BrandEntity brandEntity : brandEntityList) {
            System.out.println(brandEntity);
        }
    }

    @Test
    public void testFindPath() {
        Long[] catalogPath = categoryService.findCatalogPath(225L);
        log.info("full path: {}", Arrays.asList(catalogPath));
    }

    @Test
    public void testRedis() {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        //ops.set("hello", "haluhalu");
        System.out.println(ops.get("hello"));
    }

    @Test
    public void testRedisson() {
        System.out.println(redisson);
    }

    @Test
    public void AttrGroupTest() {
        List<SpuItemAttrGroupVo> attrGroupWithAttrsBySpuId = attrGroupService.getAttrGroupWithAttrsBySpuId(3L, 225L);
        System.out.println(attrGroupWithAttrsBySpuId);
    }

    @Test
    public void SkuItemSaleAttrVoTest() {
        List<SkuItemSaleAttrVo> saleAttrBySpuId = skuSaleAttrValueService.getSaleAttrBySpuId(3L);
        System.out.println(saleAttrBySpuId);
    }
}
