package com.pinyougou.manager.controller;
import java.util.List;

import com.pinyougou.entity.MessageInfo;
import com.pinyougou.manager.mq.MessageSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.seckill.service.SeckillGoodsService;

import com.pinyougou.entity.PageResult;
import com.pinyougou.entity.ResultInfo;
/**
 * 请求处理器
 * @author Steven
 *
 */
@RestController
@RequestMapping("/seckillGoods")
public class SeckillGoodsController {

	@Reference
	private SeckillGoodsService seckillGoodsService;

	@Autowired
	private MessageSender messageSender;
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbSeckillGoods> findAll(){			
		return seckillGoodsService.findAll();
	}
	
	
	/**
	 * 分页查询数据
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult  findPage(int pageNo,int pageSize,@RequestBody TbSeckillGoods seckillGoods){			
		return seckillGoodsService.findPage(pageNo, pageSize,seckillGoods);
	}
	
	/**
	 * 增加
	 * @param seckillGoods
	 * @return
	 */
	@RequestMapping("/add")
	public ResultInfo add(@RequestBody TbSeckillGoods seckillGoods){
		try {
			seckillGoodsService.add(seckillGoods);
			return new ResultInfo(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new ResultInfo(false, "增加失败");
		}
	}
	
	/**
	 * 修改
	 * @param seckillGoods
	 * @return
	 */
	@RequestMapping("/update")
	public ResultInfo update(@RequestBody TbSeckillGoods seckillGoods){
		try {
			seckillGoodsService.update(seckillGoods);
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
	public TbSeckillGoods getById(Long id){
		return seckillGoodsService.getById(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public ResultInfo delete(Long [] ids){
		try {
			seckillGoodsService.delete(ids);
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
	public ResultInfo updateStatus(Long[] ids,String status){
		try {
			seckillGoodsService.updateStatus(ids, status);
			//如果审核通过
			if ("1".equals(status)){
				//发送MQ消息
				messageSender.sendMessage(new MessageInfo(
						MessageInfo.METHOD_ADD, //指定增加详情标识
						ids,	//内容
						"topic-seckill-goods", //主题
						"tag-seckill-goods", //标签
						"key-seckill-goods" //唯一标识
				));
			}
			return new ResultInfo(true, "状态更新成功!");
		} catch (Exception e) {
			e.printStackTrace();
			return new ResultInfo(false, "状态更新失败!");
		}
	}
	
}
