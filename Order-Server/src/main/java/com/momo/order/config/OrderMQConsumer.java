package com.momo.order.config;

import com.google.common.base.Preconditions;
import com.momo.mq.protocol.JmsConfig;
import com.momo.mq.protocol.OrderMsg;
import com.momo.order.dao.OrderInfoMapper;
import com.momo.order.dao.ProductionMapper;
import com.momo.order.domain.OrderInfo;
import com.momo.order.domain.Production;
import com.momo.order.lock.ZkLock;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class OrderMQConsumer {
    @Autowired
    private  RedisTemplate redisTemplate;
    @Autowired
    private OrderInfoMapper orderInfoMapper;
    @Autowired
    private ProductionMapper productionMapper;
    @Autowired
    private ZkLock zkLock;
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderMQConsumer.class);
    private static final String ORDERMQCONSUMER_GROUP="ORDERMQCONSUMER_GROUP";
    private DefaultMQPushConsumer mqConsumer;
    public OrderMQConsumer() throws MQClientException {
        mqConsumer=new DefaultMQPushConsumer(ORDERMQCONSUMER_GROUP);
        mqConsumer.setNamesrvAddr(JmsConfig.NAME_SERVER);
        mqConsumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        mqConsumer.setMessageModel(MessageModel.CLUSTERING);
        mqConsumer.subscribe(JmsConfig.SECKILL_TOPIC,"*");
        mqConsumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                for (MessageExt msg : list) {
                    OrderMsg orderMsg=new OrderMsg();
                    orderMsg.decode(new String(msg.getBody()));
                    String uniqueId=orderMsg.getOrderId();
                    //分布式幂等
                    Boolean uniqueFlag=redisTemplate.opsForValue().setIfAbsent(uniqueId,"value" ,30000, TimeUnit.MILLISECONDS);
                    Boolean stockFlag=preReduceProdStock(orderMsg.getProdId());
                    if (uniqueFlag && stockFlag){
                        OrderInfo orderInfo=new OrderInfo();
                        orderInfo.setOrderId(uniqueId);
                        orderInfo.setChargeMoney(orderMsg.getChargeMoney());
                        orderInfo.setGmtCreate(new Date());
                        orderInfo.setGmtUpdate(new Date());
                        orderInfo.setOrderStatus(0);
                        orderInfo.setProdId(orderMsg.getProdId());
                        orderInfo.setUserPhoneNo(orderMsg.getUserPhoneNo());
                        orderInfoMapper.insert(orderInfo);
                        LOGGER.info("[秒杀订单消费者]-OrderMQConsumer-秒杀订单入库成功,消息消费成功!,入库实体orderInfo={}",orderInfo.toString() );
                    }else {
                        LOGGER.info("库存不足或者订单已存在");
                    }
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
                return ConsumeConcurrentlyStatus.RECONSUME_LATER;
            }
        });
        mqConsumer.start();
    }
    public boolean preReduceProdStock(String prodId) {
        Preconditions.checkNotNull(prodId, "请确保prodId非空!");
        zkLock.getLock();
        Production production = productionMapper.selectById(prodId);
        int prodStock = production.getProdStock();
        if (prodStock <= 0) {
            LOGGER.info("prodId={},prodStock={},当前秒杀商品库存已不足!", prodId, prodStock);
            zkLock.releaseLock();
            return false;
        }
        int afterPreReduce = prodStock - 1;
        // 预减库存成功,回写库存
        LOGGER.info("prodId={} 预减库存成功,当前扣除后剩余库存={}!", prodId, afterPreReduce);
        production.setProdStock(afterPreReduce);
        productionMapper.updateById(production);
        zkLock.releaseLock();
        return true;
    }
}

