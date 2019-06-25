window.onload=function () {
    var app = new Vue({
        el:"#app",
        data:{
            //购物车列表数据
            cartList:[],
            //统计业务实体{总数量，总金额}
            totalValue:{totalNum:0,totalMoney:0.0},
            //收件人列表
            addressList:[],
            //收货人信息
            address:"",
            //添加表单信息
            entity:{},
            //支付类型，1、在线支付，2、货到付款
            order:{paymentType:"1"}
        },
        methods:{
            /**
             * 提交订单
             */
            submitOrder:function(){
                this.order.receiverAreaName = this.address.address;//地址
                this.order.receiverMobile = this.address.mobile;//手机
                this.order.receiver = this.address.contact;//联系人
                axios.post("/order/add.do",this.order).then(function (response) {
                    if (response.data.success) {
                        if (app.order.paymentType == "1") {
                            window.location.href="pay.html";
                        }else {//货到付款
                            window.location.href="paysuccess.html";
                        }
                    }else{
                        alert(response.data.message);
                    }
                })
            },
            /**
             * 查询购物车数据
             */
            findCartList:function () {
                axios.get("/cart/findCartList.do").then(function (response) {
                    app.cartList = response.data;
                    //每次查询重新计算统计金额
                    app.totalValue={totalNum:0,totalMoney:0.0};
                    //统计数量于金额
                    for (var i = 0; i < app.cartList.length; i++) {
                        var cart = app.cartList[i];
                        for (var j = 0; j < cart.orderItemList.length; j++) {
                            //读取购物车明细，统计数量与金额
                            var orderItem = cart.orderItemList[j];
                            app.totalValue.totalNum += orderItem.num;
                            app.totalValue.totalMoney += orderItem.totalFee;
                        }
                    }
                })
            },
            /**
             * 修改购物车
             * @param itemId 商品ID
             * @param num 操作数量
             */
            addGoodsToCartList:function (itemId, num) {
                axios.get("/cart/addGoodsToCartList.do?itemId="+itemId+"&num="+num).then(function (response) {
                    if (response.data.success) {
                        //修改成功,刷新页面
                        app.findCartList();
                    }else {
                        alert(response.data.message);
                    }
                })
            },
        //***************************************************
            /**
             * 选择支付类型
             * @param type 1、在线支付，2、货到付款
             */
            selectPayType:function(type){
                this.order.paymentType = type;
            },
            /**
             * 跟据id删除收货人信息
             * @param id
             */
            deleteAddress:function(id){
                axios.get("/address/delete.do?id="+id).then(function (response) {
                    if (response.data.success) {
                        //刷新页面
                        app.findListByUserId();
                    }else {
                        alert(response.data.message);
                    }
                })
            },
            /**
             * 修改收货人信息
             */
            updateAddress:function(){
                var url;
                if (this.entity.id == null) {
                    url = "/address/add.do" ;
                }else{
                    url = "/address/update.do" ;
                }
                axios.post(url,this.entity).then(function (response) {
                    if (response.data.success){
                        //刷新页面
                        app.findListByUserId();
                    } else{
                        alert(response.data.message);
                    }
                })
            },
            /**
             * 跟据ID查找收货人信息
             * @param id
             */
            findAddress:function(id){
                axios.post("/address/getById.do?id="+id).then(function (response) {
                    app.entity = response.data;
                })
            },
            /**
             * 用户选择收货人信息
             */
            selectAddress:function(address){
                this.address = address;
            },
            /**
             * 查询收件人列表
             */
            findListByUserId:function () {
                axios.get("/address/findListByUserId.do").then(function (response) {
                    app.addressList = response.data;
                    //设置默认收货地址
                    for (var i = 0; i < app.addressList.length; i++) {
                        if (app.addressList[i].isDefault == '1'){
                            app.address = app.addressList[i];
                            break;
                        }
                    }
                })
            }

        },
        created:function () {
            //初始化调用查询购物车方法
            this.findCartList();
            //页面初始化调用查询收件人列表
            this.findListByUserId();
        }
    })
};