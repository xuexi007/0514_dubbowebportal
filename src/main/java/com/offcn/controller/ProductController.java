package com.offcn.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.github.pagehelper.PageInfo;
import com.offcn.entity.PageResult;
import com.offcn.entity.Result;
import com.offcn.po.Product;
import com.offcn.service.ProductService;
import com.offcn.service.ProductStockService;
import com.offcn.vo.ProductVo;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductController {

    @Reference
    ProductService productService;

    @Reference
    ProductStockService productStockService;

    @RequestMapping("getlist.do")
    @CrossOrigin(origins = "*", maxAge = 3600)
    public PageResult<ProductVo> getProductList(Integer page, Integer size){
        PageInfo<Product> pageInfo = productService.getAllProduct(page, size);
        List<Product> productList = pageInfo.getList();
        List<ProductVo> productVoList = new ArrayList<>();
        for(Product product:productList){
            ProductVo productVo = new ProductVo();
            productVo.setId(product.getId()+"");
            productVo.setName(product.getName());
            productVo.setDescs(product.getDescs());
            productVo.setPrice(product.getPrice());
            productVo.setImgurl(product.getImgurl());
            productVo.setSales(product.getSales());
            productVo.setStock(product.getStock());
            productVoList.add(productVo);

        }
        return new PageResult<ProductVo>(pageInfo.getTotal(),true,"获取商品列表成功",productVoList);
    }

    @RequestMapping("info.do")
    @CrossOrigin(origins = "*", maxAge = 3600)
    public Result getProduct(Long id){
        Product product = productService.findProductById(id);
        ProductVo productVo = new ProductVo();
        productVo.setId(product.getId()+"");
        productVo.setName(product.getName());
        productVo.setDescs(product.getDescs());
        productVo.setPrice(product.getPrice());
        productVo.setImgurl(product.getImgurl());
        productVo.setSales(product.getSales());
        productVo.setStock(product.getStock());
        return new Result(true,"商品信息查询成功",productVo);
    }
}
