package com.samchenjava.mall.search.service;

import com.samchenjava.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

public interface ProductSaveService {
    boolean productStatusOnShelf(List<SkuEsModel> skuEsModels) throws IOException;//n.133
}
