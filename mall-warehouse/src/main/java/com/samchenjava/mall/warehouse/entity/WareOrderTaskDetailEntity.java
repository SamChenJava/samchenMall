package com.samchenjava.mall.warehouse.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库存工作单
 *
 * @author samchen
 * @email nan.chen.java@gmail.com
 * @date 2020-12-30 14:17:11
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@TableName("wms_ware_order_task_detail")
public class WareOrderTaskDetailEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId
    private Long id;
    /**
     * sku_id
     */
    private Long skuId;
    /**
     * sku_name
     */
    private String skuName;
    /**
     * 购买个数
     */
    private Integer skuNum;
    /**
     * 工作单id
     */
    private Long taskId;
    /**
     * 仓库ID
     */
    private Long wareId;
    /**
     * 1-locked 2-unlocked 3-deduct
     */
    private Integer lockStatus;

}
