package com.samchenjava.mall.order.vo;

import com.samchenjava.mall.order.entity.OrderEntity;
import lombok.Data;

@Data//n.276
public class SubmitOrderRespVo {

    private OrderEntity orderEntity;
    private Integer code;//0:succeed 1:failed error code
}
