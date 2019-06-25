//页面初始化完成后再创建Vue对象
window.onload=function () {
	Vue.component('v-select',VueSelect.VueSelect);

	//创建Vue对象
	var app = new Vue({
		//接管id为app的区域
		el:"#app",
		data:{
			//声明数据列表变量，供v-for使用
			list:[],
			//总页数
			pages:1,
			//当前页
			pageNo:1,
			//声明对象
			entity:{
				//声明对象{customAttributeItems:[扩展属性列表]}
				customAttributeItems:[]
			},
			//将要删除的id列表
			ids:[],
			//搜索包装对象
			searchEntity:{},
			//品牌列表
			brandList:[{id:1,text:"联想"},{id:2,text:"TCL"},{id:3,text:"华为"}],
			//规格列表
			specList:[{id:1,text:"电冰箱"},{id:2,text:"热水器"},{id:3,text:"旧电脑"}]
		},
		methods:{
			//查询所有
			findAll:function () {
				axios.get("../typeTemplate/findAll.do").then(function (response) {
					//vue把数据列表包装在data属性中
					app.list = response.data;
				}).catch(function (err) {
					console.log(err);
				});
			},
			//分页查询
			findPage:function (pageNo) {
				axios.post("../typeTemplate/findPage.do?pageNo="+pageNo+"&pageSize="+10,this.searchEntity)
					.then(function (response) {
						app.pages = response.data.pages;  //总页数
						app.list = response.data.list;  //数据列表
						app.pageNo = pageNo;  //更新当前页
					});
			},
			//让分页插件跳转到指定页
			goPage:function (page) {
				app.$children[0].goPage(page);
			},
			//新增
			add:function () {
				var url = "../typeTemplate/add.do";
				if(this.entity.id != null){
					url = "../typeTemplate/update.do";
				}
				axios.post(url, this.entity).then(function (response) {
					if (response.data.success) {
						//刷新数据，刷新当前页
						app.findPage(app.pageNo);
					} else {
						//失败时显示失败消息
						alert(response.data.message);
					}
				});
			},
			//跟据id查询
			getById:function (id) {
				axios.get("../typeTemplate/getById.do?id="+id).then(function (response) {
					app.entity = response.data;
					//将品牌字符串转为JSON
					app.entity.brandIds = JSON.parse(app.entity.brandIds);
					//将规格字符串转换为JSON
					app.entity.specIds = JSON.parse(app.entity.specIds);
					//将自定义属性转换为JSON
					app.entity.customAttributeItems = JSON.parse(app.entity.customAttributeItems);
				})
			},
			//批量删除数据
			dele:function () {
				axios.get("../typeTemplate/delete.do?ids="+this.ids).then(function (response) {
					if(response.data.success){
						//刷新数据
						app.findPage(app.pageNo);
						//清空勾选的ids
						app.ids = [];
					}else{
						alert(response.data.message);
					}
				})
			},
			//查询品牌列表数据
			findBrandList:function () {
				axios.get("../brand/findAll.do").then(function (response) {
					//删除多余的属性
					for(var i=0;i<response.data.length;i++){
						delete response.data[i]["name"];
						delete response.data[i]["firstChar"];
					}
					//vue把数据列表包装在data属性中
					app.brandList = response.data;
				}).catch(function (err) {
					console.log(err);
				});
			},
			//查询规格列表
			findSpecList:function () {
				axios.get("../specification/findAll.do").then(function (response) {
					//删除多余的属性
					for(var i=0;i<response.data.length;i++){
						delete response.data[i]["specName"];
					}
					//vue把数据列表包装在data属性中
					app.specList = response.data;
				}).catch(function (err) {
					console.log(err);
				});
			},
			//添加属性行
			addTableRow:function () {
				this.entity.customAttributeItems.push({});
			},
			//删除属性行
			delTableRow:function (index) {
				this.entity.customAttributeItems.splice(index,1);
			},
			//优化模板列表显示
			jsonToString:function (jsonString, key) {
				var json = JSON.parse(jsonString);
				var result = "";
				for(var i=0;i<json.length;i++){
					if (i > 0) {
						result +=",";
					}
					result += json[i][key];
				}
				return result;
			}
		},
		//Vue对象初始化后，调用此逻辑
		created:function () {
			//调用用分页查询，初始化时从第1页开始查询
			this.findPage(1);
			//查询品牌
			this.findBrandList();
			//查询规格
			this.findSpecList();
		}
	});
};
