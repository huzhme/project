window.onload=function () {
    var app = new Vue({
        el:"#app",
        data:{
            //秒杀数据
            list:[],
            num:1
        },
        methods:{
            //查询秒杀页面数据
            findList:function () {
                axios.get("/seckillGoods/findList.do").then(function (response) {
                    app.list = response.data;
                })
            },
            jump:function () {
            }
        },
        created:function () {
            this.findList();
        }
    })
};