package com.momo.order.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

/**
 * 订单实体类
 */
@TableName("t_seckill_order")
public class OrderInfo {
    @TableId(type = IdType.UUID)
    private String orderId;
    private Date gmtCreate;
    private Date gmtUpdate;
    private Integer recordStatus;
    private Integer orderStatus;
    @TableField("user_phoneno")
    private String userPhoneNo;
    private String prodId;
    private String prodName;
    private String chargeMoney;
    private Date chargeTime;
    private Date finishTime;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Date getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public Date getGmtUpdate() {
        return gmtUpdate;
    }

    public void setGmtUpdate(Date gmtUpdate) {
        this.gmtUpdate = gmtUpdate;
    }

    public Integer getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(Integer orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getUserPhoneNo() {
        return userPhoneNo;
    }

    public void setUserPhoneNo(String userPhoneNo) {
        this.userPhoneNo = userPhoneNo;
    }

    public String getProdId() {
        return prodId;
    }

    public void setProdId(String prodId) {
        this.prodId = prodId;
    }

    public String getProdName() {
        return prodName;
    }

    public void setProdName(String prodName) {
        this.prodName = prodName;
    }

    public String getChargeMoney() {
        return chargeMoney;
    }

    public void setChargeMoney(String chargeMoney) {
        this.chargeMoney = chargeMoney;
    }

    public Date getChargeTime() {
        return chargeTime;
    }

    public void setChargeTime(Date chargeTime) {
        this.chargeTime = chargeTime;
    }

    public Date getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(Date finishTime) {
        this.finishTime = finishTime;
    }

    public Integer getRecordStatus() {
        return recordStatus;
    }

    public void setRecordStatus(Integer recordStatus) {
        this.recordStatus = recordStatus;
    }

    public OrderInfo() {
    }
    public OrderInfo(Date gmtCreate, Date gmtUpdate,Integer recordStatus,Integer orderStatus,
                     String userPhoneNo, String prodId, String prodName,
                     String chargeMoney, Date chargeTime, Date finishTime) {
        this.gmtCreate = gmtCreate;
        this.gmtUpdate = gmtUpdate;
        this.orderStatus = orderStatus;
        this.userPhoneNo = userPhoneNo;
        this.prodId = prodId;
        this.prodName = prodName;
        this.chargeMoney = chargeMoney;
        this.chargeTime = chargeTime;
        this.finishTime = finishTime;
    }
    @Override
    public String toString() {
        return "OrderInfo{" +
                "orderId='" + orderId + '\'' +
                ", gmtCreate=" + gmtCreate +
                ", gmtUpdate=" + gmtUpdate +
                ", orderStatus=" + orderStatus +
                ", userPhoneNo='" + userPhoneNo + '\'' +
                ", prodId='" + prodId + '\'' +
                ", prodName='" + prodName + '\'' +
                ", chargeMoney='" + chargeMoney + '\'' +
                ", chargeTime=" + chargeTime +
                ", finishTime=" + finishTime +
                '}';
    }
}
