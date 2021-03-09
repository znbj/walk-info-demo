package com.example.walkinfodemo.service.impl;

import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.walkinfodemo.common.PageParam;
import com.example.walkinfodemo.entitys.User;
import com.example.walkinfodemo.exception.BusinessException;
import com.example.walkinfodemo.mapper.UserMapper;
import com.example.walkinfodemo.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sun.security.provider.MD5;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @ClassName UserServiceImpl
 * @Author znb
 * @Date 2021-03-07 15:17
 * @Description UserServiceImpl
 * @Version 1.0
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserMapper userMapper;
    /**
     * 根据手机号查询用户
     *
     * @param phone
     */
    @Override
    public User getByPhone(String phone) {
        return baseMapper.selectOne(new QueryWrapper<User>().eq("phone", phone));
    }

    /**
     * 根据id查询用户(关联查询)
     *
     * @param userId
     */
    @Override
    public User getFullById(Integer userId) {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("userId", userId));
        return user;
    }

    /**
     * 关联分页查询用户
     *
     * @param page
     */
    @Override
    public List<User> listPage(PageParam<User> page) {

        return null;
    }

    /**
     * 添加用户(包含角色)
     *
     * @param user
     */
    @Transactional
    @Override
    public boolean saveUser(User user) {
        if (user.getPhone() != null && baseMapper.selectCount(new QueryWrapper<User>()
                .eq("phone", user.getPhone())) > 0) {
            throw new BusinessException("手机号已存在");
        }
        boolean result = baseMapper.insert(user) > 0;
        return result;
    }

    /**
     * 修改用户(包含角色)
     *
     * @param user
     */
    @Transactional
    @Override
    public boolean updateUser(User user) {
        if (user.getUsername() != null && baseMapper.selectCount(new QueryWrapper<User>()
                .eq("username", user.getUsername()).ne("user_id", user.getUserId())) > 0) {
            throw new BusinessException("账号已存在");
        }
        boolean result = baseMapper.updateById(user) > 0;
        return result;
    }

    /**
     * 比较用户密码
     *
     * @param dbPsw    数据库存储的密码
     * @param inputPsw 用户输入的密码
     * @return boolean
     */
    @Override
    public boolean comparePsw(String dbPsw, String inputPsw) {
        String s = SecureUtil.md5(inputPsw);
        return dbPsw.equals(s);
    }

    /**
     * md5加密用户密码
     *
     * @param psw
     */
    @Override
    public String encodePsw(String psw) {
        if (psw == null) return null;
        return SecureUtil.md5(psw);
    }
}
