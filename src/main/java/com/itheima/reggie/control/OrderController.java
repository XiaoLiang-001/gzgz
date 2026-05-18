package com.itheima.reggie.control;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 订单
 */
@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

 /**
  * 用户下单
  * @param orders
  * @return
  */
  @PostMapping("/submit")
  public R<String> submit(@RequestBody Orders orders){
      log.info("订单数据：{}",orders);
      orderService.submit(orders);
      return R.success("下单成功");
  }


    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize){
        //分页构造器
        Page<Orders> pageInfo = new Page<>(page,pageSize);
        //条件构造器
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        //添加排序条件，根据sort进行排序
        queryWrapper.orderByAsc(Orders::getOrderTime);
        //分页查询
        orderService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }


    /*
     * 设置订单状态
     * */
    @PutMapping
    public R<String> updateStatus(@RequestBody Map<String, Object> requestMap) {
        try {
            log.info("接收到订单更新请求: {}", requestMap);

            // 参数校验
            if (requestMap.get("id") == null || requestMap.get("status") == null) {
                return R.error("参数不完整");
            }

            // 解析参数
            Long orderId;
            Integer status;

            try {
                orderId = Long.valueOf(requestMap.get("id").toString());
                status = Integer.valueOf(requestMap.get("status").toString());
            } catch (NumberFormatException e) {
                log.error("参数格式错误: {}", e.getMessage());
                return R.error("参数格式错误");
            }

            // 验证状态值是否合法（根据业务需求）
            if (status < 2 || status > 4) {
                return R.error("状态值不合法");
            }

            //调用service
            boolean isSuccess = orderService.updateOrderStatus(orderId,status);

            if (isSuccess) {
                log.info("订单状态更新成功, 订单ID: {}, 新状态: {}", orderId, status);
                return R.success("订单状态更新成功");
            } else {
                log.warn("订单状态更新失败, 订单ID: {}", orderId);
                return R.error("订单状态更新失败");
            }

        } catch (Exception e) {
            log.error("订单状态更新异常: {}", e.getMessage(), e);
            return R.error("系统错误: " + e.getMessage());
        }
    }

}
