package com.samchenjava.mall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.samchenjava.common.utils.Query;
import com.samchenjava.mall.product.service.CategoryBrandRelationService;
import com.samchenjava.mall.product.vo.Catelog2Vo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.samchenjava.common.utils.PageUtils;

import com.samchenjava.mall.product.dao.CategoryDao;
import com.samchenjava.mall.product.entity.CategoryEntity;
import com.samchenjava.mall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //query all Categories
        List<CategoryEntity> categoryEntities = this.baseMapper.selectList(null);
        //assembled as tree structure
        List<CategoryEntity> collect = categoryEntities.stream().filter(categoryEntity ->
                categoryEntity.getParentCid() == 0
        ).map(menu -> {
            menu.setChildren(getChildren(menu, categoryEntities));
            return menu;
        }).sorted((menu1, menu2) -> {
            return menu1.getSort() - menu2.getSort();
        }).collect(Collectors.toList());
        return collect;
    }

    @Override//v50
    public void removeMenuByIds(List<Long> asList) {
        //logically delete instead of phically delete
        baseMapper.deleteBatchIds(asList);
    }

    //find full path of catelogId [parent/son] n.74
    @Override
    public Long[] findCatalogPath(Long catalogId) {
        List<Long> path = new ArrayList<>();
        List<Long> parentPath = findParentPath(catalogId, path);
        Collections.reverse(parentPath);
        return parentPath.toArray(new Long[parentPath.size()]);
    }

    //update cascade category data
    @CacheEvict(value = "category", allEntries = true) //n.171
    @Transactional
    @Override//n.75
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
    }

    @Cacheable(value = {"category"}, key = "#root.method.name", sync = true)
    //n.168 current result need to be cached. function will not be called if already exist.
    @Override//n.137
    public List<CategoryEntity> getLevel1categories() {
        List<CategoryEntity> categoryEntities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return categoryEntities;
    }

    @Cacheable(value = {"category"}, key = "#root.method.name", sync = true)
    @Override//n.153
    public Map<String, List<Catelog2Vo>> getCatalogJson() {
        //bring in cache function, data in cache is Json String
        //the data put into cache is json string, and we get json String when take them out from cache, serializable and unserializable
        //JSON is a good choice, because it is cross-language format
        List<CategoryEntity> entityList = this.list(null);
        //get level 1 categories
        List<CategoryEntity> level1categories = getParent_cid(entityList, 0L);
        //encapsulate data
        Map<String, List<Catelog2Vo>> parent_cid = level1categories.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //get level 2 categories for current level 1 category
            List<CategoryEntity> lv2Categories = getParent_cid(entityList, v.getCatId());
            //encapsulate lv2Categories
            List<Catelog2Vo> catelog2Vos = null;
            if (lv2Categories != null) {
                catelog2Vos = lv2Categories.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, v.getCatId().toString(), l2.getName());
                    //get lv3 categories of current lv2 category, encapsulating to vo
                    List<CategoryEntity> lv3Categories = getParent_cid(entityList, l2.getCatId());
                    if (lv3Categories != null) {
                        //encapsulating to customized vo
                        List<Catelog2Vo.Catelog3Vo> catelog3Vos = lv3Categories.stream().map(l3 -> {
                            return new Catelog2Vo.Catelog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(catelog3Vos);
                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2Vos;
        }));
        return parent_cid;
    }

    //n.138 get categories data from db and encapsulate it
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDb() {
        List<CategoryEntity> entityList = this.list(null);
        //get level 1 categories
        List<CategoryEntity> level1categories = getParent_cid(entityList, 0L);
        //encapsulate data
        Map<String, List<Catelog2Vo>> parent_cid = level1categories.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //get level 2 categories for current level 1 category
            List<CategoryEntity> lv2Categories = getParent_cid(entityList, v.getCatId());
            //encapsulate lv2Categories
            List<Catelog2Vo> catelog2Vos = null;
            if (lv2Categories != null) {
                catelog2Vos = lv2Categories.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, v.getCatId().toString(), l2.getName());
                    //get lv3 categories of current lv2 category, encapsulating to vo
                    List<CategoryEntity> lv3Categories = getParent_cid(entityList, l2.getCatId());
                    if (lv3Categories != null) {
                        //encapsulating to customized vo
                        List<Catelog2Vo.Catelog3Vo> catelog3Vos = lv3Categories.stream().map(l3 -> {
                            return new Catelog2Vo.Catelog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(catelog3Vos);
                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2Vos;
        }));
        return parent_cid;
    }

    /**
     * n.150
     * connect db only once and store them into entityList to improve performance
     *
     * @param entityList all category entities in db
     * @param parent_cid parent category id
     * @return
     */
    private List<CategoryEntity> getParent_cid(List<CategoryEntity> entityList, Long parent_cid) {
        return entityList.stream().filter(item -> item.getParentCid() == parent_cid).collect(Collectors.toList());
        //return this.baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", v.getCatId()));
    }

    //n.158
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithRedisLock() {

        //take distributed lock
        String uuid = UUID.randomUUID().toString();
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", "uuid", 30, TimeUnit.SECONDS);
        if (lock) {
            Map<String, List<Catelog2Vo>> dataFromDb = null;
            try {
                //get lock succeed, execute service
                dataFromDb = getDataFromDb();
            } finally {
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
                stringRedisTemplate.execute(new DefaultRedisScript<>(script, Integer.class), Arrays.asList("lock"), uuid);
            }
            //delete lock, atom.
            return dataFromDb;
        } else {
            //get lock failed
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getCatalogJsonFromDbWithRedisLock();
        }
    }

    //n.166
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithRedissonLock() {
        //take distributed lock
        RLock rLock = redissonClient.getLock("catalogJson-lock");
        rLock.lock();
        Map<String, List<Catelog2Vo>> dataFromDb = null;
        try {
            //get lock succeed, execute service
            dataFromDb = getDataFromDb();
        } finally {
            rLock.unlock();
        }
        //delete lock, atom.
        return dataFromDb;
    }

    private Map<String, List<Catelog2Vo>> getDataFromDb() {
        String catalogJson = stringRedisTemplate.opsForValue().get("catalogJson");
        if (!StringUtils.isEmpty(catalogJson)) {
            Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catelog2Vo>>>() {
            });
            return result;
        }

        List<CategoryEntity> entityList = this.list(null);
        //get level 1 categories
        List<CategoryEntity> level1categories = getParent_cid(entityList, 0L);
        //encapsulate data
        Map<String, List<Catelog2Vo>> parent_cid = level1categories.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //get level 2 categories for current level 1 category
            List<CategoryEntity> lv2Categories = getParent_cid(entityList, v.getCatId());
            //encapsulate lv2Categories
            List<Catelog2Vo> catelog2Vos = null;
            if (lv2Categories != null) {
                catelog2Vos = lv2Categories.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, v.getCatId().toString(), l2.getName());
                    //get lv3 categories of current lv2 category, encapsulating to vo
                    List<CategoryEntity> lv3Categories = getParent_cid(entityList, l2.getCatId());
                    if (lv3Categories != null) {
                        //encapsulating to customized vo
                        List<Catelog2Vo.Catelog3Vo> catelog3Vos = lv3Categories.stream().map(l3 -> {
                            return new Catelog2Vo.Catelog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(catelog3Vos);
                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2Vos;
        }));
        return parent_cid;
    }

    private List<Long> findParentPath(Long catalogId, List<Long> path) {
        path.add(catalogId);
        CategoryEntity byId = this.getById(catalogId);
        if (byId.getParentCid() != 0) {
            findParentPath(byId.getParentCid(), path);
        }
        return path;
    }

    //recursion get all subcategory for current category
    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all) {
        List<CategoryEntity> collect = all.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid().equals(root.getCatId());
        }).map(categoryEntity -> {
            categoryEntity.setChildren(getChildren(categoryEntity, all));
            return categoryEntity;
        }).sorted((menu1, menu2) -> {
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());
        return collect;
    }

}