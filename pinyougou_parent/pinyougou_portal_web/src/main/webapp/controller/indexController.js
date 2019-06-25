var app = new Vue({
    el: "#app",
    data: {
        //广告列表
        contentList: [],
        keyword:''
    },
    methods: {
        //查询广告列表
        findContentList: function () {
            //查询广告轮播图
            axios.get("/content/findByCategoryId.do?categoryId=1").then(function (response) {
                app.$set(app.contentList,0,response.data);
            })
        },
        /**
         * 搜索跳转
         */
        search:function () {
            window.location.href = "http://localhost:8083/search.html?keyword="+this.keyword;
        }
    },
    //初始化调用
    created: function () {
        //查询广告列表
        this.findContentList();
    }
});
