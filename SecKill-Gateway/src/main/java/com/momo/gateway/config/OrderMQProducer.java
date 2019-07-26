package com.momo.gateway.config;

import com.alibaba.fastjson.JSON;
import com.momo.gateway.domain.Production;
import com.momo.gateway.utils.CodeMsg;
import com.momo.gateway.utils.Result;
import com.momo.mq.protocol.JmsConfig;
import com.momo.mq.protocol.OrderMsg;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;
@Component
public class OrderMQProducer {
    @Autowired
    private RedisTemplate<Object,Object> redisTemplate;
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderMQProducer.class);
    private static final String ORDERMQPRODUCER_GROUD="ORDERMQPRODUCER_GROUD";
    private DefaultMQProducer mqProducer;
    public OrderMQProducer() throws MQClientException {
        mqProducer=new DefaultMQProducer(ORDERMQPRODUCER_GROUD);
        mqProducer.setNamesrvAddr(JmsConfig.NAME_SERVER);
        mqProducer.start();
    }
    public Result secKillOrderEnqueue(String prodId){
        String orderId = UUID.randomUUID().toString();
        String phoneNo ="123456789";
        String Price = ((Production)redisTemplate.opsForValue().get(prodId)).getProdPrice().toString();
        OrderMsg orderMsg=new OrderMsg();
        orderMsg.setProdId(prodId);
        orderMsg.setOrderId(orderId);
        orderMsg.setUserPhoneNo(phoneNo);
        orderMsg.setChargeMoney(Price);
        String msgBody = orderMsg.encode();
        LOGGER.info("秒杀订单入队,消息协议={}", msgBody);
        Message msg =new Message(JmsConfig.SECKILL_TOPIC,"tag1",orderId,msgBody.getBytes());
        try {
          SendResult sendResult= mqProducer.send(msg);
          if(sendResult == null || sendResult.getSendStatus() != SendStatus.SEND_OK){
              LOGGER.error("秒杀订单消息投递失败,下单失败.msgBody={},sendResult=null",msgBody);
              return Result.error(CodeMsg.BIZ_ERROR);
          }

            LOGGER.info("秒杀订单消息投递成功,订单入队.出参sendResult={}",sendResult.toString());
            return Result.success(CodeMsg.ORDER_INLINE,"");
        } catch (Exception e) {
            int sendRetryTimes = mqProducer.getRetryTimesWhenSendFailed();
            LOGGER.error("sendRetryTimes={},秒杀订单消息投递异常,下单失败.msgBody={},e={}", sendRetryTimes, msgBody,e.toString());
        }
        return Result.error(CodeMsg.BIZ_ERROR);
    }
}
