package com.momo.gateway.controller;
import com.momo.gateway.config.OrderMQProducer;
import com.momo.gateway.init.SecKillProductConfig;
import com.momo.gateway.utils.CodeMsg;
import com.momo.gateway.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SecKillController {
    @Autowired
    private SecKillProductConfig secKillProductConfig;
    @Autowired
    private OrderMQProducer orderMQProducer;
    @RequestMapping("/secKill")
    public Result secKill(String prodId){
        //检查商品是否存在
        if (secKillProductConfig.checkProductionExist(prodId)) {
            return Result.error(CodeMsg.PRODUCT_NOT_EXIST);
        }
        // 前置预减库存
        if (secKillProductConfig.preReduceProdStock(prodId)) {
            return orderMQProducer.secKillOrderEnqueue(prodId);
        }else {
            return Result.error(CodeMsg.PRODUCT_STOCK_NOT_ENOUGH);
        }
    }
}
