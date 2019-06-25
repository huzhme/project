//创建Vue对象
var app = new Vue({
	//接管id为app的区域
	el: "#app",
	data: {
		//声明数据列表变量，供v-for使用
		list: [],
		//总页数
		pages: 1,
		//当前页
		pageNo: 1,
		/*声明对象{
          goods:{typeTemplateId:模板Id},
          goodsDesc:{itemImages:[图片列表],customAttributeItems:[扩展属性列表],
                     specificationItems:[用户勾选规格列表]}
       }*/
		entity: {
			goods: {typeTemplateId: 0},
			goodsDesc: {
				itemImages: [],
				customAttributeItems: [],
				specificationItems: []
			},
			itemList:[]
		},
		//将要删除的id列表
		ids: [],
		//搜索包装对象
		searchEntity: {auditStatus:"0"},
		//图片上传成功保存的对象
		image_entity: {
			url: ""
		},
		//商品分类
		//一级分类
		itemCatList1: [],
		//二级分类
		itemCatList2: [],
		//三级分类
		itemCatList3: [],
		//品牌列表信息
		brandIds: [],
		//规格列表
		specIds: [],
		//商品分类对象
		itemCatMap:{},
		//商品状态
		status:['未审核','已审核','审核未通过','关闭']
	},
	//监听变量的变化触发某些逻辑
	watch: {
		"entity.goods.category1Id": function (newValue) {
			//查询二级分类
			this.findItemList(newValue, 2);
			//清空三级下拉框
			this.itemCatList3 = [];
			//重置模板Id设置为0
			this.entity.goods.typeTemplateId = 0;
		},
		"entity.goods.category2Id": function (newValue) {
			//查询三级分类
			this.findItemList(newValue, 3);
			//重置模板Id设置为0
			this.entity.goods.typeTemplateId = 0;
		},
		"entity.goods.category3Id": function (newValue) {
			//当三级分类变量变化后，查询模板Id
			axios.get("/itemCat/getById.do?id=" + newValue).then(function (value) {
				app.entity.goods.typeTemplateId = value.data.typeId;
			})
		},
		//当模板id变量变化后，查询模板信息,从而查找品牌信息
		"entity.goods.typeTemplateId": function (newValue) {
			axios.get("/typeTemplate/getById.do?id=" + newValue).then(function (value) {
				//品牌列表
				app.brandIds = JSON.parse(value.data.brandIds);
			});

		}
	},
	methods: {
		//查询所有
		findAll: function () {
			axios.get("../goods/findAll.do").then(function (response) {
				//vue把数据列表包装在data属性中
				app.list = response.data;
			}).catch(function (err) {
				console.log(err);
			});
		},
		//分页查询
		findPage: function (pageNo) {
			axios.post("../goods/findPage.do?pageNo=" + pageNo + "&pageSize=" + 10, this.searchEntity)
				.then(function (response) {
					app.pages = response.data.pages;  //总页数
					app.list = response.data.list;  //数据列表
					app.pageNo = pageNo;  //更新当前页
				});
		},
		//让分页插件跳转到指定页
		goPage: function (page) {
			app.$children[0].goPage(page);
		},
		//新增
		add: function () {
			var url = "../goods/add.do";
			if (this.entity.goods.id != null) {
				url = "../goods/update.do";
			}
			//获取富文本内容
			this.entity.goodsDesc.introduction = editor.html();

			axios.post(url, this.entity).then(function (response) {
				if (response.data.success) {
					//成功时提示
					alert(response.data.message);
					//刷新数据，刷新当前页
					app.entity = {goods: {}, goodsDesc: {}};
					//清空富文本编辑器
					editor.html("");
					location.href="goods.html";
				} else {
					//失败时显示失败消息
					alert(response.data.message);
				}
			});
		},
		//跟据id查询
		getById: function () {
			//获取url上的id
			var id = this.getUrlParam()['id'];
			if (id != null && id.length > 0) {
				axios.get("../goods/getById.do?id=" + id).then(function (value) {
					app.entity = value.data;
					//绑定富文本内容
					editor.html(app.entity.goodsDesc.introduction);
					//把图片json字符串转换成对象
					app.entity.goodsDesc.itemImages = JSON.parse(app.entity.goodsDesc.itemImages);
					//把拓展属性json字符串转换成对象
					app.entity.goodsDesc.customAttributeItems = JSON.parse(app.entity.goodsDesc.customAttributeItems);
					//把sku属性json字符串转换成对象
					app.entity.goodsDesc.specificationItems = JSON.parse(app.entity.goodsDesc.specificationItems);
					//SKU列表规格列转换
					for (var i = 0; i < app.entity.itemList.length; i ++){
						app.entity.itemList[i].spec = JSON.parse(app.entity.itemList[i].spec);
					}
				})
			}
		},
		//批量删除数据
		dele: function () {
			axios.get("../goods/delete.do?ids=" + this.ids).then(function (response) {
				if (response.data.success) {
					//刷新数据
					app.findPage(app.pageNo);
					//清空勾选的ids
					app.ids = [];
				} else {
					alert(response.data.message);
				}
			})
		},
		//查询商品分类(父节点id,当前查询的分类级别)
		findItemList: function (parentId, grade) {
			axios.get("/itemCat/findByParentId.do?parentId=" + parentId).then(function (value) {
				app['itemCatList' + grade] = value.data;
			})
		},
		/**
		 * 查找一个集合数组中某个属性的值是否存在
		 * @param list
		 * @param key
		 * @param keyValue
		 * @return 存在：返回这个集合数组
		 * @return 不存在：返回null
		 */
		searchObjectByKey: function (list, key, keyValue) {
			for (var i = 0; i < list.length; i++) {
				//如果找到相应属性的值，返回找到的对象
				if (list[i][key] == keyValue) {
					return list[i];
				}
			}
			//找不到返回空
			return null;
		},
		//查询所有商品分类
		findAllItemCat:function () {
			axios.get("/itemCat/findAll.do").then(function (value) {
				//组装商品分类数组[id:商品分类名称]
				for (var i = 0; i < value.data.length; i ++){
					app.$set(app.itemCatMap,value.data[i].id,value.data[i].name);
				}
			})
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
		},
		/**
		 验证规格选项是否要勾选
		 * @param specName 规格名称
		 * @param optionName 规格项名称
		 * @returns {查询找结果}
		 */
		checkAttributeValue:function (specName, optionName) {
			var item = this.entity.goodsDesc.specificationItems;
			//先查询规格名称是否存在
			var obj = this.searchObjectByKey(item,'attributeName',specName);
			if (obj != null) {
				//如果找到了相应规格项，注意此处是>-1
				if (obj.attributeValue.indexOf(optionName) > -1 ){
					return true;
				}
			}
			return false;
		},
		//审核操作
		updateStatus:function (status) {
			axios.get("/goods/updateStatus.do?ids="+this.ids+"&status="+status).then(function (value) {
				if (value.data.success) {
					//刷新数据
					app.findPage(app.pageNo);
					//清空勾选的ids
					app.ids=[]
				}else{
					alert(value.data.message)
				}
			})
		}

	},
	//Vue对象初始化后，调用此逻辑
	created: function () {
		//调用用分页查询，初始化时从第1页开始查询
		this.findPage(1);
		//查询第一级商品分类
		this.findItemList(0, 1);
		//查询所有商品分类
		this.findAllItemCat();
		//跟据id查询商品信息
		this.getById();
	}
});
