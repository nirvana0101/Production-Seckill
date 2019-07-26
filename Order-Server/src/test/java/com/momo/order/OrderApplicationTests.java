package com.momo.order;

import com.momo.order.dao.OrderInfoMapper;
import com.momo.order.dao.ProductionMapper;
import com.momo.order.domain.OrderInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
public class OrderApplicationTests {
    @Autowired
    private OrderInfoMapper orderInfoMapper;
    @Test
    public void contextLoads() {
        OrderInfo orderInfo=new OrderInfo(new Date(),new Date(),0,
                1,"11","11","11","11",
                new Date(),new Date());
        orderInfoMapper.insert(orderInfo);
    }
    @Test
    public void testInsert() {

    }
}
