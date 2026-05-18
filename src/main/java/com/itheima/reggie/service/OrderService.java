package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.Orders;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface OrderService extends IService<Orders> {
    /**
     * 用户下单
     * @param orders
     */
    public void submit(Orders orders);

    // 设置订单状态
    boolean updateOrderStatus(Long orderId, Integer status);
}
