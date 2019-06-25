window.onload=function () {
    var app = new Vue({
        el:"#app",
        data:{
            //购买数量
            num:1,
            //记录用户选择的规格
            specificationItems: {},
            //将要购买的商品
            sku:{}
        },
        methods:{
            /**
             * 添加购物车
             */
            addToCart:function(){
                axios.get("http://localhost:8086/cart/addGoodsToCartList.do?itemId="+this.sku.id+"&num="+this.num,{'withCredentials':true}).then(function (response) {
                    if (response.data.success){
                        //跳转到购物车页面
                        window.location.href = "http://localhost:8086/cart.html";
                    }else{
                        alert(response.data.message)
                    }
                })
            },
            /**
             * 商品数量的添加
             * @param x 数量
             */
            addNum:function (x) {
                this.num = x;
                if(this.num < 1){
                    this.num = 1;
                }
            },
            /**
             * 用户选择规格
             * @param specName 规格名称
             * @param optionName 选项名称
             */
            selectSpecification:function (specName, optionName) {
                this.$set(this.specificationItems,specName,optionName);
                //更新sku
                this.searchSku();
            },
            /**
             * 判断某规格选项是否被用户选中
             * @param specName 规格名称
             * @param optionName 选项名称
             * @return {boolean}
             */
            isSelected:function (specName, optionName) {
                return this.specificationItems[specName] == optionName;
            },
            /**
             * 加载默认要购买的商品
             */
            loadSku:function () {
                //记录默认要购买的商品，由于后台跟据默认排序，所以这里第一条就是默认商品
                this.sku = skuList[0];
                //选中默认的规格，注意这里要用深克隆
                this.specificationItems = JSON.parse(JSON.stringify(this.sku.spec));
            },
            /**
             * 匹配两个对象的内容是否一致
             * @param map1
             * @param map2
             * @return {boolean}
             */
            matchObject:function (map1, map2) {
                for (var k in map1) {
                    if(map1[k] != map2[k]){
                        return false;
                    }
                }
                for (var k in map2) {
                    if(map2[k] != map1[k]){
                        return false;
                    }
                }
                return true;
            },
            /**
             * 选择商品规格后,找到对应的商品信息加入购物车
             */
            searchSku:function () {
                for (var i = 0; i < skuList.length; i++) {
                    //用户选择的规格信息和skuList列表的规格信息对比
                    if (this.matchObject(skuList[i].spec, this.specificationItems)) {
                        //将包含对应信息的规格列表添加到变量sku中
                        this.sku = skuList[i];
                        return;
                    }
                }
                //如果没有匹配信息
                this.sku={
                    "id":0,
                    "title":"无",
                    "price":"无",
                    "spec":0
                }
            }
        },
        //初始化调用
        created:function () {
            //默认选中第一个商品
            this.loadSku();
        }
    });

};