window.onload = function () {
    var app = new Vue({
        el: "#app",
        data: {
            //查询结果集
            resultMap: {brandList:[]},
            //搜索条件集{keywords: 关键字, category: 商品分类, brand: 品牌,
            //          spec: {'网络'：'移动4G','机身内存':'64G',price 价格}
            //          sortField:排序域名,sort:排序方式asc|desc}
            searchMap: {
                keyword: '',
                category: '',
                brand: '',
                spec: {},
                price: '',
                pageNo: 1,   //当前页
                pageSize: 20, //每页显示的条数
                sortField:'', //排序域名
                sort:'' //排序方式asc|desc
            },
            //pageLabel:[分页标签列表] [1,2,3,4,5]
            pageLabel: [],
            //标识分页插件中是否显示前面的省略号
            firstDot:true,
            //标识分页插件中是否显示后面的省略号
            lastDot:true,
            //用于记录查询按钮点击后searchMap.keyword的值
            //searchKeyword:''(这里不使用老师的方法,使用搜索的searchMap.keyword)
        },
        methods: {
            //搜索数据
            searchList: function () {
                axios.post("/esItem/search.do", this.searchMap).then(function (response) {
                    app.resultMap = response.data;
                    //刷新分页标签
                    app.buildPageLabel();
                })
            },
            /**
             * 添加搜索项
             * @param key
             * @param value
             */
            addSearchItem: function (key, value) {
                //如果点击的是分类或者是品牌
                if (key == 'category' || key == 'brand' || key == 'price') {
                    app.$set(app.searchMap, key, value)
                } else {//否则是规格
                    app.$set(app.searchMap.spec, key, value);
                }
                //刷新数据
                this.searchList();
            },
            /**
             * 跟据key删除搜索项
             * @param key
             */
            removeSearchItem: function (key) {
                //如果点击的是分类或者是品牌
                if (key == 'category' || key == 'brand' || key == 'price') {
                    app.$set(app.searchMap, key, '')
                } else {//否则是规格
                    app.$delete(app.searchMap.spec, key);
                }
                //刷新数据
                this.searchList();
            },
            //构建分页标签
            buildPageLabel: function () {
                //每次重新构建需要清空当前数组
                this.pageLabel = [];
                var begin = 1;//开始页码
                var end = this.resultMap.totalPages;//结束页码
                //总页数>5
                if (this.resultMap.totalPages > 5) {
                    //如果当前页码 <= 3，显示前5页
                    if (this.searchMap.pageNo <= 3) {
                        end = 5;
                        this.firstDot = false;//不显示前面的...
                    } else if ( this.searchMap.pageNo >= (this.resultMap.totalPages-2)) {
                        //如果当前页码 >= (总页数-2)，显示后5页
                        begin = this.resultMap.totalPages - 4;
                        this.lastDot = false;//不显示后面的...
                    } else {//显示当前页为中心的5个页码
                        begin = this.searchMap.pageNo - 2;
                        end = this.searchMap.pageNo + 2;
                        this.firstDot = true;//显示前面的...
                        this.lastDot = true;//显示后面的...
                    }
                }else{
                    this.firstDot = false;//不显示前面的...
                    this.lastDot = false;//不显示后面的...
                }
                for (var i = begin; i <= end; i++) {
                    this.pageLabel.push(i);
                }
            },
            /**
             * 页面跳转点击事件
             * @param pageNo 当前要跳转的页数
             */
            queryByPage: function (pageNo) {
                //先把参数转换为Int
                pageNo = parseInt(pageNo);
                if (pageNo < 1 || pageNo > this.resultMap.totalPages) {
                    alert("错误页码！");
                    return;
                }
                //修改当前页
                this.searchMap.pageNo = pageNo;
                //刷新数据
                this.searchList();
            },
            /**
             *排序查询
             * @param sort 排序方式 asc,desc
             * @param sortField 排序域名
             */
            sortSearch:function (sort, sortField) {
                this.searchMap.sort = sort;
                this.searchMap.sortField = sortField;
                //刷新页面
                this.searchList();
            },
            /**
             * 搜索的关键字是否包含品牌
             * @return {结果 true|false}
             */
            keywordIsBrand:function () {
                for (var i = 0; i < this.resultMap.brandList.length; i++) {
                    //如果包含品牌,则返回true
                    if (this.searchMap.keyword.indexOf(this.resultMap.brandList[i].text) > -1) {
                        return true;
                    }
                }
                return false;
            },
            /**
             * 接收参数并进行查询
             */
            loadkeyword:function(){
                //读取参数
                var keyword = this.getUrlParam()['keyword'];
                if (keyword != null) {
                    //解码
                    keyword = decodeURI(keyword);
                    //decodeURI-把url的中文转换回来
                    this.searchMap.keyword = keyword;
                }
                //刷新数据
                this.searchList();
            },
            /**
             * 解析一个url中所有的参数
             * @return {参数名:参数值}
             */
            getUrlParam:function() {
                //url上的所有参数
                var paramMap = {};
                //获取当前页面的url
                var url = document.location.toString();
                //获取问号后面的参数
                var arrObj = url.split("?");
                //如果有参数
                if (arrObj.length > 1) {
                    //解析问号后的参数
                    var arrParam = arrObj[1].split("&");
                    //读取到的每一个参数,解析成数组
                    var arr;
                    for (var i = 0; i < arrParam.length; i++) {
                        //以等于号解析参数：[0]是参数名，[1]是参数值
                        arr = arrParam[i].split("=");
                        if (arr != null) {
                            paramMap[arr[0]] = arr[1];
                        }
                    }
                }
                return paramMap;
            }

        },
        //初始化调用
        created: function () {
            this.searchList();
            //读取关键字查询
            this.loadkeyword();
        }
    });

};