//页面初始化完成后再创建Vue对象
window.onload=function () {
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
			entity:{},
			//将要删除的id列表
			ids:[],
			//搜索包装对象
			searchEntity:{},
			//记录当前面包屑的级别
			grade:1,
			//一级分类
			entity_1:{id:0,name:"顶级列表"},
			//二级分类
			entity_2:{},
			//三级分类
			entity_3:{},
			//记录父ID
			parentId:0
		},
		methods:{
			//查询所有
			findAll:function () {
				axios.get("../itemCat/findAll.do").then(function (response) {
					//vue把数据列表包装在data属性中
					app.list = response.data;
				}).catch(function (err) {
					console.log(err);
				});
			},
			//分页查询
			findPage:function (pageNo) {
				axios.post("../itemCat/findPage.do?pageNo="+pageNo+"&pageSize="+10,this.searchEntity)
					.then(function (response) {
						app.pages = response.data.pages;  //总页数
						app.list = response.data.rows;  //数据列表
						app.pageNo = pageNo;  //更新当前页
					});
			},
			//让分页插件跳转到指定页
			goPage:function (page) {
				app.$children[0].goPage(page);
			},
			//新增
			add:function () {
				var url = "../itemCat/add.do";
				//记录父ID
				this.entity.parentId = this.parentId;
				if(this.entity.id != null){
					url = "../itemCat/update.do";
				}
				axios.post(url, this.entity).then(function (response) {
					if (response.data.success) {
						//刷新数据，刷新当前页
						app.findByParentId({id:app.parentId});
						//设置当前节点等级
						app.grade--;
					} else {
						//失败时显示失败消息
						alert(response.data.message);
					}
				});
			},
			//跟据id查询
			getById:function (id) {
				axios.get("../itemCat/getById.do?id="+id).then(function (response) {
					app.entity = response.data;
				})
			},
			//批量删除数据
			dele:function () {
				axios.get("../itemCat/delete.do?ids="+this.ids).then(function (response) {
					if(response.data.success){
						//刷新数据，刷新当前页
						app.findByParentId({id:app.parentId});
						//设置当前节点等级
						app.grade--;
						//清空勾选的ids
						app.ids = [];
					}else{
						alert(response.data.message);
					}
				})
			},
			//根据父ID查找商品分类列表
			findByParentId:function (p_entity) {
				//将查询值的ID作为父ID传递
				this.parentId = p_entity.id;
				//当前在一级分类下
				if (this.grade === 1){
					//清空2，3级面包屑
					this.entity_2={};
					this.entity_3={};
				}else if (this.grade === 2){
					this.entity_2 = p_entity;
					//清空3级面包屑
					this.entity_3 = {};
				} else {
					this.entity_3 = p_entity;
				}
				//查询一次下一级，记录一次当前面包屑级别
				this.grade++;
				axios.get("/itemCat/findByParentId.do?parentId="+p_entity.id).then(function (value) {
					app.list = value.data;
				})
			}
		},
		//Vue对象初始化后，调用此逻辑
		created:function () {
			//调用用分页查询，初始化时从第1页开始查询
			this.findByParentId(this.entity_1);
		}
	});
};
