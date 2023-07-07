package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.SMSUtils;
import com.itheima.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session) {
        //获取手机号
        String phone = user.getPhone();
        if (StringUtils.isNotEmpty(phone)) {
            // 生成验证码
            String code = ValidateCodeUtils.generateValidateCode(6).toString();
            log.info(code);
            // 调用阿里云短信服务给指定手机号发送验证码
            // SMSUtils.sendMessage("瑞吉外卖", "", phone, code);   //由于没有注册阿里云，暂时无法使用该服务
            // 使用session对象来保存验证码，以此在登录对前端提交的验证码进行校验
            session.setAttribute(phone, code);
            return R.success("手机验证码发送成功");
        }
        return R.error("验证码发送失败");
    }

    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session) { // User类中没有code字段，而前端传来的json数据本质上也是键值对，所以可以用 Map 集合来接收
        String phone = map.get("phone").toString();
        String code = map.get("code").toString();
        String codeInsession = (String)session.getAttribute(phone);  // 获取用户填写的验证码
        if (codeInsession!=null && codeInsession.equals(code)){ // 将用户填写的验证码与之前保存在session中的验证进行校验，校验通过则判断该用户是否已经注册
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone);
            User user = userService.getOne(queryWrapper);
            if (user == null){ // 用户之前没有注册，则先将用户信息自动注册保存
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            session.setAttribute("user",user.getId());
            return R.success(user); //登录成功！
        }
        return R.error("验证码输入错误！");
    }
}
