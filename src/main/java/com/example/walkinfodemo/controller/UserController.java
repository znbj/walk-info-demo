package com.example.walkinfodemo.controller;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.example.walkinfodemo.common.JsonResult;
import com.example.walkinfodemo.entitys.User;
import com.example.walkinfodemo.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Objects;

@CrossOrigin
@RestController
@Slf4j
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public JsonResult login(String phone, String password) {
        User byPhone = userService.getByPhone(phone);
        if (byPhone != null) {
            boolean b = userService.comparePsw(byPhone.getPassword(), password);
            if (b) {
                return JsonResult.ok(JSON.toJSONString(byPhone));
            } else {
                return JsonResult.error("账户或密码不正确");
            }
        }
        return JsonResult.error("该手机号未注册");
    }

    @PostMapping("/reg")
    public JsonResult reg(String phone, String password) {
        User byPhone = userService.getByPhone(phone);
        if (byPhone == null) {
            User user = new User();
            user.setPhone(phone);
            user.setPassword(userService.encodePsw(password));
            user.setCreateTime(new Date());
            user.setUsername(UUID.fastUUID().toString(true));
            userService.saveUser(user);
            return JsonResult.ok();
        }
        return JsonResult.error("该手机号已注册");
    }

    /**
     * 保存实名信息
     * @param user
     * @return
     */
    @PostMapping("/updateNameInfo")
    public JsonResult update(User user) {
        if (Objects.nonNull(user) && StrUtil.isNotEmpty(String.valueOf(user.getUserId()))) {
            user.setAuditStatus(0);
            user.setUpdateTime(new Date());
            boolean b = userService.saveOrUpdate(user);
            if (b) {
                return JsonResult.ok();
            } else {
                return JsonResult.error("保存失败");
            }
        }
        return JsonResult.error("用户信息为空");
    }

}
