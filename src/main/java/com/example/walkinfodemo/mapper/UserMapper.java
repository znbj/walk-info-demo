package com.example.walkinfodemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.walkinfodemo.common.PageParam;
import com.example.walkinfodemo.entitys.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 用户Mapper接口
 * Created by wangfan on 2018-12-24 16:10
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {


}
