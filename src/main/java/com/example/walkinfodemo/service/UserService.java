package com.example.walkinfodemo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.walkinfodemo.common.PageParam;
import com.example.walkinfodemo.entitys.User;

import java.util.List;
import java.util.Map;

/**
 * @ClassName UserService
 * @Author znb
 * @Date 2021-03-07 15:14
 * @Description UserService
 * @Version 1.0
 */
public interface UserService extends IService<User> {

    /**
     * 根据手机号查询用户
     */
    User getByPhone(String phone);

    /**
     * 根据id查询用户(关联查询)
     */
    User getFullById(Integer userId);


    /**
     * 关联分页查询用户
     */
    List<User> listPage(PageParam<User> page);


    /**
     * 添加用户(包含角色)
     */
    boolean saveUser(User user);

    /**
     * 修改用户(包含角色)
     */
    boolean updateUser(User user);

    /**
     * 比较用户密码
     *
     * @param dbPsw    数据库存储的密码
     * @param inputPsw 用户输入的密码
     * @return boolean
     */
    boolean comparePsw(String dbPsw, String inputPsw);

    /**
     * md5加密用户密码
     */
    String encodePsw(String psw);

}
