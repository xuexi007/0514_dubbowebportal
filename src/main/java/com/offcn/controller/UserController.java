package com.offcn.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.offcn.entity.Result;
import com.offcn.po.User;
import com.offcn.service.UserService;
import com.offcn.util.IdWorker;
import com.offcn.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sun.misc.BASE64Encoder;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@RestController
public class UserController {

    @Reference
    UserService userService;

    @Autowired
    HttpServletRequest request;

    @Autowired
    IdWorker idWorker;

    @Autowired
    RedisTemplate redisTemplate;


    //用户登录
    @RequestMapping("login.do")
    @CrossOrigin(origins = "*", maxAge = 3600)
    public Result login(String username, String password) {
        if (username == null || password == null) {
            return new Result(false, "登录失败，账号密码未填写", null);
        } else {
            User loginUser = null;
            try {
                loginUser = userService.login(username, EncodeByMD5(password));
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            if (loginUser != null) {
                //把登录结果写入到session
                // request.getSession().setAttribute("IS_LOGIN",true);
                //  request.getSession().setAttribute("LOGIN_USER",loginUser);
                String jwt = JwtUtil.generateToken(username);
                return new Result(true, "登录成功", jwt);
            }
        }

        return new Result(false, "登录失败");
    }


    //生成短信验证码
    @RequestMapping("/makesmscode.do")
    @CrossOrigin(origins = "*", maxAge = 3600)
    public Result makeSmsCode(String telephone) {
        //生成随机的验证码
        Random random = new Random();
        Integer randomOtp = random.nextInt(99999);
        randomOtp = randomOtp + 10000;
        String randomOtpCode = String.valueOf(randomOtp);

        //将验证码和手机号存储到redis,设置有效期1分钟

        redisTemplate.opsForValue().set(telephone, randomOtpCode,60000, TimeUnit.MILLISECONDS);

        System.out.println("telephone=" + telephone + "toptCode=" + randomOtpCode);
        //预留短信发送接口调用
        return new Result(true, "验证码发送成功");
    }

    //用户注册
    @RequestMapping("/user/register.do")
    @CrossOrigin(origins = "*", maxAge = 3600)
    public Result register(User user) {
        //0、验证码是否输入相同
        String smscode = (String) redisTemplate.opsForValue().get(user.getTelephone());
        if (smscode != null && smscode.equals(user.getSmscode())) {
            //1、使用id生成器生成id
            long id = idWorker.nextId();
            user.setId(id);
            //2、加密密码
            try {
                String encodeByMD5 = EncodeByMD5(user.getEncrptPassword());
                user.setEncrptPassword(encodeByMD5);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

            userService.registerUser(user);
            return new Result(true, "用户注册成功");
        } else {
            return new Result(false, "验证码不正确");
        }

    }


    //对用户注册时输入的密码进行MD5方式的加密
    public String EncodeByMD5(String password) throws NoSuchAlgorithmException {
        //确定计算方法
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        BASE64Encoder base64Decoder = new BASE64Encoder();

        //加密字符串
        String encrptPassword = base64Decoder.encode(md5.digest(password.getBytes()));

        return encrptPassword;
    }

    //获取指定id的用户信息
    @RequestMapping("/api/user/getuser.do")
    @CrossOrigin(origins = "*", maxAge = 3600)
    public Result getUser(Integer id) {

        try {
            User user = userService.findUserById(id);

            if (user != null) {
                return new Result(true, "用户信息获取成功", user);
            } else {
                return new Result(false, "用户信息获取失败");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Result(false, "用户信息获取失败");
    }

    //验证Token有效性
    @RequestMapping("validatetoken.do")
    @CrossOrigin(origins = "*", maxAge = 3600)
    public Result validatetoken(String token) {
        if (token == null || token.equals("")) {
            return new Result(false, "token验证失败，token未填写");
        } else {

            Map<String, Object> map = null;
            try {
                map = JwtUtil.validateToken(token);
                Object username = map.get("username");
                if (username != null && !username.equals("")) {
                    return new Result(true, "token验证成功", username);
                }
            } catch (Exception e) {
                // e.printStackTrace();

            }

        }
        return new Result(false, "token验证失败");
    }
}
