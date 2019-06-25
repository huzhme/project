package com.pinyougou.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.entity.PageResult;
import com.pinyougou.entity.QueueTag;
import com.pinyougou.entity.ResultInfo;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 请求处理器
 * @author xiaobo
 *
 */
@RestController
@RequestMapping("/seckillOrder")
public class SeckillOrderController {

	@Reference
	private SeckillOrderService seckillOrderService;
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbSeckillOrder> findAll(){			
		return seckillOrderService.findAll();
	}
	
	
	/**
	 * 分页查询数据
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult  findPage(int pageNo,int pageSize,@RequestBody TbSeckillOrder seckillOrder){			
		return seckillOrderService.findPage(pageNo, pageSize,seckillOrder);
	}
	
	/**
	 * 增加
	 * @param seckillOrder
	 * @return
	 */
	@RequestMapping("/add")
	public ResultInfo add(@RequestBody TbSeckillOrder seckillOrder){
		try {
			seckillOrderService.add(seckillOrder);
			return new ResultInfo(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new ResultInfo(false, "增加失败");
		}
	}
	
	/**
	 * 修改
	 * @param seckillOrder
	 * @return
	 */
	@RequestMapping("/update")
	public ResultInfo update(@RequestBody TbSeckillOrder seckillOrder){
		try {
			seckillOrderService.update(seckillOrder);
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
	public TbSeckillOrder getById(Long id){
		return seckillOrderService.getById(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public ResultInfo delete(Long [] ids){
		try {
			seckillOrderService.delete(ids);
			return new ResultInfo(true, "删除成功"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new ResultInfo(false, "删除失败");
		}
	}

	/**
	 * 立即抢购
	 */
	@RequestMapping("/submitOrder")
	public ResultInfo submitOrder(Long seckillId){
		//获取登入的用户名
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		if ("anonymousUser".equals(userId)){
			return new ResultInfo(false, "请先登入!");
		}
		try {
			seckillOrderService.submitOrder(seckillId,userId);
			return new ResultInfo(true, "请稍后,系统正在为您抢单!");
		} catch (RuntimeException e) {
			e.printStackTrace();
			//e.getMessage() == 当前商品，您已存在未付款订单，请先支付！
			return new ResultInfo(false, e.getMessage());
		}
	}

	/**
	 * 跟据用户ID和商品ID查询排队标识状态
	 * @param seckillId
	 * @return
	 */
	@RequestMapping("/getQueueStatus")
	public ResultInfo getQueueStatus(Long seckillId){
		try {
			while (true){
				//获取登入的用户名
				String userId = SecurityContextHolder.getContext().getAuthentication().getName();
				QueueTag queueStatus = seckillOrderService.getQueueStatus(userId, seckillId);
				switch (queueStatus){
					case CREATE_ORDER:
						return new ResultInfo(true, "抢购成功，请5分钟内完成支付！");
					case NO_STOCK:
						return new ResultInfo(false, "你来晚了,商品已被抢购完毕!");
					case SECKILL_FAIL:
						return new ResultInfo(false, "抢购当前商品的人数过多，请稍后再试!");
				}
				//3秒查询一次
				Thread.sleep(3000);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return new ResultInfo(false, "查询排队状态失败!");
	}
}
