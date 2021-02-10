package com.samchenjava.mall.product.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.plugins.pagination.optimize.JsqlParserCountOptimize;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

//n.75
@EnableTransactionManagement
@Configuration
@MapperScan("com.samchenjava.mall.product.dao")
public class MybatisPlusConfig {

    //import mybatis-plus springboot plugin
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
        // 设置请求的页面大于最大页后操作， true调回到首页，false 继续请求  默认false
        // page will redirect to first page, if request page number greater than our largest page number when parameter set to true
        paginationInterceptor.setOverflow(true);
        // 设置最大单页限制数量，默认 500 条，-1 不受限制
        // set page size for a single page, 500 by default, no limit if -1
        paginationInterceptor.setLimit(500);
        // 开启 count 的 join 优化,只针对部分 left join
        return paginationInterceptor;
    }
}