package com.momo.gateway.init;
import com.google.common.base.Preconditions;
import com.momo.gateway.dao.ProductionMapper;
import com.momo.gateway.domain.Production;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class SecKillProductConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecKillProductConfig.class);
    @Autowired
    private ProductionMapper productionMapper;
    @Autowired
    private RedisTemplate<Object,Object> redisTemplate;
    @PostConstruct
    public void init() {
        List<Production> killProductList =productionMapper.selectList(null);
        if (killProductList == null) {
            throw new RuntimeException("请确保数据库中存在秒杀商品配置!");
        }
        for (Production production:killProductList){
            redisTemplate.opsForValue().set(production.getProdId(),production);
        }
        LOGGER.info("[SecKillProductConfig]初始化秒杀配置完成,商品信息=[{}]", redisTemplate);
    }

    /**
     * 预减库存
     * @param prodId
     * @return
     */
    public boolean preReduceProdStock(String prodId) {
        Preconditions.checkNotNull(prodId, "请确保prodId非空!");
        synchronized (this) {
            Production production = (Production)redisTemplate.opsForValue().get(prodId);
            int prodStock = production.getProdStock();
            if (prodStock <= 0) {
                LOGGER.info("prodId={},prodStock={},当前秒杀商品库存已不足!", prodId, prodStock);
                return false;
            }
            int afterPreReduce = prodStock - 1;
            // 预减库存成功,回写库存
            LOGGER.info("prodId={} 预减库存成功,当前扣除后剩余库存={}!", prodId, afterPreReduce);
            production.setProdStock(afterPreReduce);
            redisTemplate.opsForValue().set(prodId,production);
            return true;
        }
    }

    public boolean checkProductionExist(String prodId) {
        if(redisTemplate.opsForValue().get(prodId)==null){
            return true;
        }
        return false;
    }
}
