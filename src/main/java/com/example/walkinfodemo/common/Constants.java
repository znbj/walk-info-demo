package com.example.walkinfodemo.common;

/**
 * 系统常量
 * Created by wangfan on 2019-10-29 15:55
 */
public class Constants {
    /* 返回结果统一 */
    public static final int RESULT_OK_CODE = 0;  // 默认成功码
    public static final int RESULT_ERROR_CODE = 1;  // 默认失败码

    /* 其他 */
    public static final Long TOKEN_EXPIRE_TIME = 60 * 60 * 24L;  // token过期时间,单位秒
    public static final int TOKEN_WILL_EXPIRE = 30;  // token将要过期自动刷新,单位分钟
    public static final String TOKEN_KEY = "ULgNsWJ8rPjRtnjzX/Gv2RGS80Ksnm/ZaLpvIL+NrBg=";  // 生成token的key


}
