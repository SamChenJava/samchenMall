package com.samchenjava.mall.product.web;

import com.samchenjava.mall.product.entity.CategoryEntity;
import com.samchenjava.mall.product.service.CategoryService;
import com.samchenjava.mall.product.vo.Catelog2Vo;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class IndexController {

    @Autowired
    CategoryService categoryService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @GetMapping({"/", "/index.html"})//n.137
    public String indexPage(Model model) {
        List<CategoryEntity> cat1Entities = categoryService.getLevel1categories();
        model.addAttribute("categories", cat1Entities);
        return "index";
    }

    @ResponseBody//n.138
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catelog2Vo>> getCataglogJson() {
        Map<String, List<Catelog2Vo>> map = categoryService.getCatalogJson();
        return map;
    }

}
