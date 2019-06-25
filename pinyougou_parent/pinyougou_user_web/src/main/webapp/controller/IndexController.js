window.onload=function () {
    var app = new Vue({
        el:"#app",
        data:{
            loginName:""
        },
        methods:{
            /**
             * 获取用户名
             */
            loginNameInfo:function () {
                axios.get("/login/name.do").then(function (response) {
                    app.loginName = response.data.username;
                })
            }

        },
        created:function () {
            //初始化调用 用户名
            this.loginNameInfo();
        }
    })
};