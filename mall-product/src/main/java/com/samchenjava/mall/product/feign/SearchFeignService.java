package com.samchenjava.mall.product.feign;

import com.samchenjava.common.to.es.SkuEsModel;
import com.samchenjava.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("mall-search")
public interface SearchFeignService {
    @PostMapping("/search/save/product")
//n.133
    R productStatusShelf(@RequestBody List<SkuEsModel> skuEsModels);
}
