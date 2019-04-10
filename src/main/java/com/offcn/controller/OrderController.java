package com.offcn.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.github.pagehelper.PageInfo;
import com.offcn.entity.PageResult;
import com.offcn.entity.Result;
import com.offcn.po.Order;
import com.offcn.po.Product;
import com.offcn.po.User;
import com.offcn.service.OrderService;
import com.offcn.service.ProductService;
import com.offcn.service.UserService;
import com.offcn.util.IdWorker;
import com.offcn.util.RedisLock;
import com.offcn.vo.OrderVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    HttpServletRequest request;

    @Autowired
    IdWorker idWorker;

    @Reference
    OrderService orderService;

    @Reference
    ProductService productService;

    @Reference
    UserService userService;
    @Autowired
    RedisLock redisLock;
    @RequestMapping("createOrder.do")
    @CrossOrigin(origins = "*", maxAge = 3600)
    public Result createOrder(Long productId,Integer amount){


        //1、获取登录用户
        String username= (String) request.getAttribute("LOGIN_USER");
        User user = userService.getUserByUsername(username);
        // System.out.println("username:"+username+" productid:"+productId);
        //2、获取商品详情
        Product product = productService.findProductById(productId);

        //System.out.println("商品信息:"+product);
        //3、保存订单
        Double total=amount*product.getPrice();
        Order order = new Order(idWorker.nextId(), user.getId(), productId, product.getPrice(), amount, total, 0);
        Integer num = 0;
        String appid="createOrderLock";
        try {
            //获取redis分布式锁
            long ex = 5 * 1000L;

            String value = String.valueOf(System.currentTimeMillis() + ex);
            boolean lock = redisLock.lock(appid, value);
            //如果能获取到锁，就执行下单操作
            if(lock) {
                num = orderService.createOrder(order);
            }
            //释放锁
            redisLock.unlock(appid, value);
        } catch (Exception e) {
            System.out.println("下单失败");
            //e.printStackTrace();
            return new Result(false,"下订单失败,服务器异常","stockisnull");
        }
        if(num>0) {
            return new Result(true, "订单保存成功");
        }else {
            return new Result(false,"下订单失败,库存不足","stockisnull");
        }
    }

    @RequestMapping("getall.do")
    @CrossOrigin(origins = "*", maxAge = 3600)
    public PageResult<OrderVo> getallOrder(){

        PageInfo<Order> pageInfo = orderService.getall(1, 100);
        List<Order> list = pageInfo.getList();
        ArrayList<OrderVo> listOrderVo = new ArrayList<>();
        for (Order order:list){
            OrderVo orderVo = new OrderVo();
            orderVo.setId(order.getId()+"");
            orderVo.setAmount(order.getAmount());
            orderVo.setPrice(order.getPrice());
            //根据商品id获取商品名称
            Product product = productService.findProductById(order.getProductid());
            orderVo.setProductname(product.getName());
            orderVo.setTotal(order.getTotal());
            Integer paystatus = order.getPaystatus();
            if(paystatus==0){
                orderVo.setStatu("未支付");
            }else if(paystatus==1){
                orderVo.setStatu("支付完成");
            }else {
                orderVo.setStatu("支付未完成");
            }

            listOrderVo.add(orderVo);
        }
        return new PageResult<OrderVo>(pageInfo.getTotal(),true,"订单列表",listOrderVo);
    }

    //获取订单详情
    @RequestMapping("info.do")
    @CrossOrigin(origins = "*", maxAge = 3600)
    public Result getorderInfo(Long orderid){
        System.out.println("orderid:"+orderid);
        Order order = null;
        OrderVo orderVo=new OrderVo();
        try {
            order = orderService.getOrderById(orderid);
            orderVo.setId(order.getId()+"");
            orderVo.setAmount(order.getAmount());
            orderVo.setPrice(order.getPrice());
            //根据商品id获取商品名称
            Product product = productService.findProductById(order.getProductid());
            orderVo.setProductname(product.getName());
            orderVo.setTotal(order.getTotal());
            Integer paystatus = order.getPaystatus();
            if(paystatus==0){
                orderVo.setStatu("未支付");
            }else if(paystatus==1){
                orderVo.setStatu("支付完成");
            }else {
                orderVo.setStatu("支付未完成");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return  new Result(false,"获取订单失败");
        }

        return  new Result(true,"获取订单成功",orderVo);
    }

    //更新订单状态
    @RequestMapping("update.do")
    @CrossOrigin(origins = "*", maxAge = 3600)
    public Result updateOrderStatus(Long id, Integer paystatus){

        Integer updateOrderStatusNum = orderService.updateOrderStatus(id, paystatus);
        if(updateOrderStatusNum>0){
            return new Result(true,"更新订单状态成功");
        }else {
            return new Result(false,"更新订单状态失败");
        }
    }
}
