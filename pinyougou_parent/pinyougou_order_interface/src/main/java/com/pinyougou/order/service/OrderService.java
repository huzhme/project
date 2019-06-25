package com.pinyougou.order.service;
import java.util.List;
import com.pinyougou.pojo.TbOrder;

import com.pinyougou.entity.PageResult;
import com.pinyougou.pojo.TbPayLog;

/**
 * 业务逻辑接口
 * @author xiaobo
 *
 */
public interface OrderService {

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbOrder> findAll();
	
	
	/**
     * 分页查询列表
     * @return
     */
    public PageResult<TbOrder> findPage(int pageNum, int pageSize,TbOrder order);
	
	
	/**
	 * 增加
	*/
	public void add(TbOrder order);
	
	
	/**
	 * 修改
	 */
	public void update(TbOrder order);
	

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public TbOrder getById(Long id);
	
	
	/**
	 * 批量删除
	 * @param ids
	 */
	public void delete(Long [] ids);

	/**
	 * 跟据用户Id查找用户订单支付日志
	 * @param userId
	 * @return
	 */
	public TbPayLog searchPayLogFromRedis(String userId);

	/**
	 * 跟据订单号修改日志信息和关联的订单状态
	 * @param out_trade_no
	 * @param transaction_id
	 */
	public void updateOrderStatus(String out_trade_no,String transaction_id);

}
