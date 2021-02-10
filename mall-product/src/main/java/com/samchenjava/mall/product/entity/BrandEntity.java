package com.samchenjava.mall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

import com.samchenjava.common.valid.AddGroup;
import com.samchenjava.common.valid.ListValue;
import com.samchenjava.common.valid.UpdateGroup;
import com.samchenjava.common.valid.UpdateStatusGroup;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;

/**
 * 品牌
 *
 * @author samchen
 * @email nan.chen.java@gmail.com
 * @date 2020-12-29 17:49:09
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * brandId
     */
    @NotNull(message = "brandId must not be empty when update", groups = {UpdateGroup.class})
    @Null(message = "brandId must be empty when create", groups = {AddGroup.class})
    @TableId
    private Long brandId;
    /**
     * brand name
     */
    @NotBlank(message = "brand name must not be empty", groups = {AddGroup.class, UpdateGroup.class})
    private String name;
    /**
     * URL of brand logo
     */
    @NotBlank(groups = {AddGroup.class})
    @URL(message = "logo must be a valid url", groups = {AddGroup.class, UpdateGroup.class})
    private String logo;
    /**
     * description
     */
    private String descript;

    /*n.69
     * show status [0: not display, 1: display]
     * */
    @NotNull(groups = {AddGroup.class, UpdateStatusGroup.class})
    @ListValue(vals = {0, 1}, groups = {AddGroup.class, UpdateStatusGroup.class})//customize validation
    private Integer showStatus;

    @NotEmpty(groups = {AddGroup.class})
    @Pattern(regexp = "^[a-zA-Z]$", message = "first letter must between a-z or A-Z", groups = {AddGroup.class, UpdateGroup.class})
    private String firstLetter;

    @NotNull(groups = {AddGroup.class})
    @Min(value = 0, message = "order number must be greater than or equal 0", groups = {AddGroup.class, UpdateGroup.class})
    private Integer sort;

}
