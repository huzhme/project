package com.pinyougou.manager.controller;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.pinyougou.entity.EsItem;
import com.pinyougou.entity.MessageInfo;
import com.pinyougou.manager.mq.MessageSender;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojogroup.Goods;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.service.GoodsService;

import com.pinyougou.entity.PageResult;
import com.pinyougou.entity.ResultInfo;
/**
 * 请求处理器
 * @author Steven
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

	@Reference
	private GoodsService goodsService;

	/*@Reference
	private EsItemService esItemService;*/

	/*@Reference
	private ItemPageService itemPageService;*/

	@Autowired
	private MessageSender messageSender;
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll(){			
		return goodsService.findAll();
	}
	
	
	/**
	 * 分页查询数据
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult  findPage(int pageNo,int pageSize,@RequestBody TbGoods goods){			
		return goodsService.findPage(pageNo, pageSize,goods);
	}
	
	/**
	 * 增加
	 * @param goods
	 * @return
	 */
	@RequestMapping("/add")
	public ResultInfo add(@RequestBody Goods goods){
		try {
			//获取当前登陆商家的用户名
			String username = SecurityContextHolder.getContext().getAuthentication().getName();
			goods.getGoods().setSellerId(username);
			goodsService.add(goods);
			return new ResultInfo(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new ResultInfo(false, "增加失败");
		}
	}
	
	/**
	 * 修改
	 * @param goods
	 * @return
	 */
	@RequestMapping("/update")
	public ResultInfo update(@RequestBody Goods goods){
		try {
			goodsService.update(goods);
			return new ResultInfo(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new ResultInfo(false, "修改失败");
		}
	}	
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/getById")
	public Goods getById(Long id){
		return goodsService.getById(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public ResultInfo delete(Long [] ids){
		try {
			goodsService.delete(ids);
			//删除索引库
			//esItemService.deleteByGoodsId(ids);
			//发送MQ消息(删除索引库)
			MessageInfo info = new MessageInfo(
					MessageInfo.METHOD_DELETE,
					ids,
					"topic-goods",
					"tags-goods-delete",
					"keys-goods-delete"
			);
			messageSender.sendMessage(info);

			return new ResultInfo(true, "删除成功"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new ResultInfo(false, "删除失败");
		}
	}

    /**
     * 跟据id列表，更新状态
     * @param ids
     * @param status
     */
    @RequestMapping("/updateStatus")
    public ResultInfo updateStatus(Long[] ids, String status) {
        try {
            goodsService.updateStatus(ids,status);
            //如果审核通过
			if ("1".equals(status)){
				//查询SKU列表
				List<TbItem> items = goodsService.findItemListByGoodsIdsAndStatus(ids, status);
				if (items !=null && items.size()>0) {
					//es数据实体列表
					List<EsItem> esItemList = new ArrayList<>();
					EsItem esItem = null;
					for (TbItem item : items) {
						esItem = new EsItem();
						//使用spring的BeanUtils深克隆对象
						//相当于把TbItem所有属性名与数据类型相同的属性值设置到EsItem中
						BeanUtils.copyProperties(item, esItem);
						//注意，这里价格的类型不一样需要单独设置
						esItem.setPrice(item.getPrice().doubleValue());
						//嵌套域-规格数据绑定
						Map specMap = JSON.parseObject(item.getSpec(), Map.class);
						esItem.setSpec(specMap);
						//组装es实体列表
						esItemList.add(esItem);
					}
					//1.导入索引库
					//esItemService.importList(esItemList);
					//发送MQ消息
					MessageInfo info = new MessageInfo(
							MessageInfo.METHOD_ADD,
							esItemList,
							"topic-goods",
							"tags-goods-add",
							"keys-goods-add"
					);
					messageSender.sendMessage(info);

					/*//2.使用FreeMarker技术生成商品静态化页面
					for (Long id : ids) {
						itemPageService.getItemHtml(id);
					}
*/
				}else {
					System.out.println("没有找到SKU数据");
				}
			}
            return new ResultInfo(true, "更新成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultInfo(false, "更新失败");
        }
    }

	/**
	 * 生成静态页（测试）
	 * @param goodsId
	 */
	/*@RequestMapping("/getHtml")
	public boolean getHtml(Long goodsId){
		return itemPageService.getItemHtml(goodsId);
	}*/

}
