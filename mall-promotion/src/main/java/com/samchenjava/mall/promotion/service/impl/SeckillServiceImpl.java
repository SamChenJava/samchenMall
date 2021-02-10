package com.samchenjava.mall.promotion.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.samchenjava.common.to.mq.SeckillOrderTo;
import com.samchenjava.common.utils.R;
import com.samchenjava.common.vo.MemberRespVo;
import com.samchenjava.mall.promotion.feign.CouponFeignService;
import com.samchenjava.mall.promotion.feign.ProductFeignService;
import com.samchenjava.mall.promotion.interceptor.LoginUserInterceptor;
import com.samchenjava.mall.promotion.service.SeckillService;
import com.samchenjava.mall.promotion.to.SecKillSkuRedisTo;
import com.samchenjava.mall.promotion.vo.SeckillSessionsWithSkusVo;
import com.samchenjava.mall.promotion.vo.SkuInfoVo;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SeckillServiceImpl implements SeckillService {

    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    RabbitTemplate rabbitTemplate;

    private final String SESSIONS_CACHING_PREFIX = "seckill:sessions:";

    private final String SKU_SECKILL_CACHING_PREFIX = "seckill:skus";

    private final String SKU_STOCK_SEMAPHORE = "seckill:stock:";//product random code

    @Override//n.313
    public void shelfSecKillSkusInNext3Days() {
        //scan skus which are envolved in next 3 days
        R session = couponFeignService.getNext3DaysSessions();
        if (session.getCode() == 0) {
            //shelf skus
            List<SeckillSessionsWithSkusVo> sessionData = session.getData(new TypeReference<List<SeckillSessionsWithSkusVo>>() {
            });
            //put sessions and skus in redis
            //caching second kill promotions info
            saveSessionInfos(sessionData);
            //caching second kill promotions skus info
            saveSessionSkuInfos(sessionData);
        }

    }

    @Override//n.318
    public List<SecKillSkuRedisTo> getCurrentSeckillSkus() {
        long time = new Date().getTime();
        Set<String> keys = stringRedisTemplate.keys(SESSIONS_CACHING_PREFIX + "*");
        List<SecKillSkuRedisTo> collect = new ArrayList<>();
        for (String key : keys) {
            String replace = key.replace(SESSIONS_CACHING_PREFIX, "");
            String[] s = replace.split("_");
            Long start = Long.parseLong(s[0]);
            Long end = Long.parseLong(s[1]);

            if (time >= start && time <= end) {
                List<String> range = stringRedisTemplate.opsForList().range(key, -100, 100);
                BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(SKU_SECKILL_CACHING_PREFIX);
                List<String> list = hashOps.multiGet(range);
                if (list != null) {
                    List<SecKillSkuRedisTo> subCollect = list.stream().map(item -> {
                        SecKillSkuRedisTo secKillSkuRedisTo = JSON.parseObject(item.toString(), SecKillSkuRedisTo.class);
                        return secKillSkuRedisTo;
                    }).collect(Collectors.toList());
                    collect.addAll(subCollect);
                }
            }
        }
        return collect;
    }

    @Override//n.319
    public SecKillSkuRedisTo getSkuSeckillInfo(Long skuId) {
        BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(SKU_SECKILL_CACHING_PREFIX);
        Set<String> keys = hashOps.keys();
        if (keys != null && keys.size() > 0) {
            String regx = "\\d_" + skuId;
            for (String key : keys) {
                if (Pattern.matches(regx, key)) {
                    String jsonString = hashOps.get(key);
                    SecKillSkuRedisTo secKillSkuRedisTo = JSON.parseObject(jsonString, SecKillSkuRedisTo.class);
                    long time = new Date().getTime();
                    if (time < secKillSkuRedisTo.getStartTime() && time > secKillSkuRedisTo.getEndTime()) {
                        secKillSkuRedisTo.setRandomCode(null);
                    }
                    return secKillSkuRedisTo;
                }
            }
        }
        return null;
    }

    //7_1 like data
    @Override//v322
    public String kill(String killId, String key, Integer num) {
        MemberRespVo memberRespVo = LoginUserInterceptor.loginUser.get();
        //get current promotion skus infos
        BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(SKU_SECKILL_CACHING_PREFIX);
        String json = hashOps.get(killId);
        if (StringUtils.isEmpty(json)) {
            return null;
        } else {
            SecKillSkuRedisTo redisTo = JSON.parseObject(json, SecKillSkuRedisTo.class);
            //validation
            Long startTime = redisTo.getStartTime();
            Long endTime = redisTo.getEndTime();
            Long now = new Date().getTime();
            Long ttl = endTime - now;
            if (now >= startTime && now <= endTime) {
                //verify random code and skuID
                String randomCode = redisTo.getRandomCode();
                String skuId = redisTo.getPromotionSessionId() + "_" + redisTo.getSkuId();
                if (randomCode.equals(key) && killId.equals(skuId)) {
                    //verify customer purchase quantity
                    BigDecimal seckillLimit = redisTo.getSeckillLimit();
                    BigDecimal killCount = new BigDecimal(num + "");
                    if (killCount.compareTo(seckillLimit) == -1 || killCount.compareTo(seckillLimit) == 0) {
                        //idempotence, occupy succeed is set value succeed。 userId_SessionId_skuId
                        String occupyKey = memberRespVo.getId() + "_" + skuId;
                        //automatic expire
                        Boolean aBoolean = stringRedisTemplate.opsForValue().setIfAbsent(occupyKey, num.toString(), ttl, TimeUnit.MILLISECONDS);
                        if (aBoolean) {
                            //customer has not been purchased before if occupy succeed
                            RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + randomCode);
                            //boolean b = semaphore.tryAcquire(num, 100, TimeUnit.MILLISECONDS);
                            boolean b = semaphore.tryAcquire(num);
                            if (b) {
                                //purchase succeed
                                //place order by sending rabbitmq messages v323
                                String timeId = IdWorker.getTimeId();
                                SeckillOrderTo orderTo = new SeckillOrderTo();
                                orderTo.setOrderSn(timeId);
                                orderTo.setMemberId(memberRespVo.getId());
                                orderTo.setNum(new BigDecimal(num + ""));
                                orderTo.setPromotionSessionId(redisTo.getPromotionSessionId());
                                orderTo.setSkuId(redisTo.getSkuId());
                                orderTo.setSeckillPrice(redisTo.getSeckillPrice());
                                rabbitTemplate.convertAndSend("order-event-exchange", "order.seckill.order", orderTo);
                                return timeId;
                            }
                            return null;
                        } else {
                            //already purchased
                            return null;
                        }
                    }
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
        return null;
    }

    //n.314
    private void saveSessionInfos(List<SeckillSessionsWithSkusVo> sessions) {
        sessions.stream().forEach(session -> {
            Long startTime = session.getStartTime().getTime();
            Long endTime = session.getEndTime().getTime();
            String key = SESSIONS_CACHING_PREFIX + startTime + "_" + endTime;
            Boolean hasKey = stringRedisTemplate.hasKey(key);
            if (!hasKey) {//n.317 idempotence
                List<String> skuIds = session.getRelationSkus().stream().map(sku -> sku.getPromotionSessionId() + "_" + sku.getSkuId().toString()).collect(Collectors.toList());
                stringRedisTemplate.opsForList().leftPushAll(key, skuIds);
            }
        });
    }

    //n.314 315 317
    private void saveSessionSkuInfos(List<SeckillSessionsWithSkusVo> sessions) {
        sessions.stream().forEach(session -> {
            BoundHashOperations<String, Object, Object> hashOps = stringRedisTemplate.boundHashOps(SKU_SECKILL_CACHING_PREFIX);
            session.getRelationSkus().stream().forEach(seckillSkuVo -> {
                // random code uri: seckill?skuId=1&key=adadfsdf;
                String token = UUID.randomUUID().toString().replace("-", "");
                if (!hashOps.hasKey(seckillSkuVo.getPromotionSessionId().toString() + "_" + seckillSkuVo.getSkuId().toString())) {
                    SecKillSkuRedisTo secKillSkuRedisTo = new SecKillSkuRedisTo();
                    //sku basic info
                    R skuInfoResp = productFeignService.getSkuInfo(seckillSkuVo.getSkuId());
                    SkuInfoVo skuInfoVo = skuInfoResp.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                    });
                    secKillSkuRedisTo.setSkuInfo(skuInfoVo);
                    //sku second kill info
                    BeanUtils.copyProperties(seckillSkuVo, secKillSkuRedisTo);
                    //set current sku promotion period info
                    secKillSkuRedisTo.setStartTime(session.getStartTime().getTime());
                    secKillSkuRedisTo.setEndTime(session.getEndTime().getTime());

                    secKillSkuRedisTo.setRandomCode(token);

                    String jsonString = JSON.toJSONString(secKillSkuRedisTo);
                    hashOps.put(seckillSkuVo.getPromotionSessionId().toString() + "_" + seckillSkuVo.getSkuId().toString(), jsonString);//save skus info

                    //distributed redisson -> semaphore, flow limitation
                    RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + token);
                    //take current sku quantity as semaphore
                    semaphore.trySetPermits(secKillSkuRedisTo.getSeckillCount().intValue());
                }

            });
        });
    }
}
