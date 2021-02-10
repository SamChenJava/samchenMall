package com.samchenjava.mall.promotion.scheduler;

import com.samchenjava.mall.promotion.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service//n.313
public class SeckillSkuScheduler {

    @Autowired
    SeckillService seckillService;

    @Autowired
    RedissonClient redissonClient;//n.317 Idempotence

    private final String upload_lock = "seckill:upload:lock";

    /**Idempotence
     * shelf second kill skus in next 3 Days
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void shelfSecKillSkusInNext3Days(){
        log.info("shelf second kill product");
        //distributed lock
        RLock lock = redissonClient.getLock(upload_lock);
        lock.lock(10, TimeUnit.SECONDS);
        try{
            seckillService.shelfSecKillSkusInNext3Days();
        }finally {
            lock.unlock();
        }
    }
}
