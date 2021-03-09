// JavaScript Document


/*-------注册验证-----------*/
$().ready(function() {
    $("#signupForm").validate({
        rules: {
            telphone:{
                required: true,
                rangelength:[11,11],
                digits: "只能输入整数"
            },
            telphone2:{

                rangelength:[11,11],
                digits: "只能输入整数"
            },
            password: {
                required: true,
                rangelength:[8,20]
            },
            confirm_password: {
                required: true,
                equalTo: "#password",
                rangelength:[8,20]
            },
            sljb: {
                required: true,
            },
            id_card:{
                required: true,
                rangelength:[18,18],
                digits: "只能输入整数"
            },
            real_name2:{
                required: true
            }
        },
        messages: {
            telphone:{
                required: "请输入手机号",
                rangelength: jQuery.format("请输入正确的手机号"),
            },
            telphone2:{

                rangelength: jQuery.format("<br/>请输入正确的推荐人手机号"),
            },
            password: {
                required: "请输入密码",
                rangelength: jQuery.format("密码在8~20位之间"),
            },
            confirm_password: {
                required: "请输入确认密码",
                rangelength: jQuery.format("密码在8~20个字符之间"),
                equalTo: "两次输入密码不一致"
            },
            sljb: {
                required: "请选择注册级别",
            },
            id_card:{
                required: "请输入证件号码",
                rangelength: jQuery.format("请输入正确的身份证号"),
            },
            real_name2:{
                required: "请输入姓名",
            }
        }
    });
});
