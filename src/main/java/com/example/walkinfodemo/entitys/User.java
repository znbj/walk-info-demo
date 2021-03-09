package com.example.walkinfodemo.entitys;


import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.Date;



@ApiModel(description = "用户")
@TableName("walk_user")
@Data
public class User {

    @ApiModelProperty("用户id")
    @TableId(value = "user_id", type = IdType.AUTO)
    private Integer userId;

    @TableField(value = "username")
    @ApiModelProperty("账号")
    private String username;

    @TableField(value = "password")
    @ApiModelProperty("密码")
    private String password;

    @TableField(value = "imageUrl")
    @ApiModelProperty("头像url")
    private String imageUrl;

    @TableField(value = "phone")
    @ApiModelProperty("手机号")
    private String phone;

    @TableField(value = "trueName")
    @ApiModelProperty("真实姓名")
    private String trueName;

    @TableField(value = "introduction")
    @ApiModelProperty("个人简介")
    private String introduction;

    @TableField(value = "state")
    @ApiModelProperty("状态，0正常，1冻结")
    private Integer state;

    @TableField(value = "isRealName")
    @ApiModelProperty("是否实名,0否,1是")
    private Integer isRealName;

    @TableField(value = "createTime")
    @ApiModelProperty("注册时间")
    private Date createTime;

    @TableField(value = "updateTime")
    @ApiModelProperty("修改时间")
    private Date updateTime;

    @TableField(value = "coin")
    @ApiModelProperty("金币数量")
    private String coin;

    @TableField(value = "lateSignIn")
    @ApiModelProperty("最后登录日期")
    private Date lateSignIn;

    @TableField(value = "auditStatus")
    @ApiModelProperty("审核状态:0审核中,1审核通过,2不合格,3未实名")
    private Integer auditStatus;

    //    IDImageUrl
    @TableField(value = "idImageUrl")
    @ApiModelProperty("身份证图片")
    private String idImageUrl;

    @TableField(value = "remarks")
    private String remarks;
}
