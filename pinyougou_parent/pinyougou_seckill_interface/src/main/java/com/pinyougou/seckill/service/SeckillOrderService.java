package com.pinyougou.seckill.service;

import com.pinyougou.entity.PageResult;
import com.pinyougou.entity.QueueTag;
import com.pinyougou.pojo.TbSeckillOrder;

import java.util.List;

/**
 * 业务逻辑接口
 * @author Steven
 *
 */
public interface SeckillOrderService {

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbSeckillOrder> findAll();
	
	
	/**
     * 分页查询列表
     * @return
     */
    public PageResult<TbSeckillOrder> findPage(int pageNum, int pageSize, TbSeckillOrder seckillOrder);


	/**
	 * 增加
	*/
	public void add(TbSeckillOrder seckillOrder);


	/**
	 * 修改
	 */
	public void update(TbSeckillOrder seckillOrder);


	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public TbSeckillOrder getById(Long id);


	/**
	 * 批量删除
	 * @param ids
	 */
	public void delete(Long[] ids);


	/**
	 * 立即抢购
	 * @param seckillId 抢购商品ID
	 * @param userId	用户ID
	 */
	public void submitOrder(Long seckillId,String userId);

	/**
	 * 跟据用户ID和商品ID查询排队标识状态
	 * @param userId
	 * @param seckillId
	 * @return
	 */
	public QueueTag getQueueStatus(String userId,Long seckillId);

	/**
	 * 跟据用户名查询秒杀订单
	 * @param userId 用户名
	 * @return
	 */
	public TbSeckillOrder searchOrderFromRedisByUserId(String userId);

	/**
	 * 跟据用户名获取秒杀订单信息保存到数据库
	 * @param userId
	 * @param transaction_id
	 */
	public void saveOrderFromRedisToDb(String userId,String transaction_id);

	/**
	 * 从缓存中删除订单并还原库存(订单超时)
	 * @param userId
	 * @param orderId
	 */
	public void deleteOrderFromRedis(String userId,Long orderId);
	
}
