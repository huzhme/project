window.onload=function () {
    var app = new Vue({
        el:"#app",
        data:{
            //页面数据
            entity:{},
            //确认密码
            pwd:"",
            //验证码
            code:""
        },
        methods:{
            /**
             * 注册方法
             */
            reg:function () {
                //确认两次密码是否一致
                if (this.entity.password != this.pwd) {
                    alert("两次输入的密码不一致!");
                    return;
                }
                if (this.code == "") {
                    alert("请输入验证码!");
                    return;
                }
                //注册
                axios.post("/user/add.do?code="+this.code,this.entity).then(function (response) {
                    alert(response.data.message);
                })
            },
            /**
             * 发送验证码
             */
            sendCode:function () {
                if (this.entity.phone == null) {
                    alert("请输入手机号!")
                }
                axios.get("/user/sendCode.do?phone="+this.entity.phone).then(function (response) {
                    alert(response.data.message);
                })
            }

        },
        created:function () {

        }
    })
};