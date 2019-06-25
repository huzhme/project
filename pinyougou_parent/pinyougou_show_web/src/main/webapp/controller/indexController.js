window.onload=function () {
  var vm = new Vue({
      el:"#app",
      data:{
        loginName:""
      },
      methods:{
          getLoginInfo:function () {
              axios.get("../login/info.do").then(function (response) {
                  vm.loginName = response.data.loginName;
              })
          }
      },
      //vue对象初始化时，调用该钩子
      created:function () {
          //获取登入名
          this.getLoginInfo();
      }
  })
};