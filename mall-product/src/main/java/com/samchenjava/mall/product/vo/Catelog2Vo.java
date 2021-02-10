package com.samchenjava.mall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
//catalog level 2 vo
public class Catelog2Vo {
    private String catalog1Id;  //1级父分类id
    private List<Catelog3Vo> catalog3List;  //3级子分类
    private String id;
    private String name;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    /**
     * catalog level 3 vo
     * store catalog level 3 as static inner class
     * 三级catalog写成静态内部类
     */
    public static class Catelog3Vo {
        private String catalog2Id;  //父分类，2级分类id
        private String id;
        private String name;
    }
}
