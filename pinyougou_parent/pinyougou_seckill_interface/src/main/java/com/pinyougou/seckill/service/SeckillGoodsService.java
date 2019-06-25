package com.pinyougou.seckill.service;

import com.pinyougou.entity.PageResult;
import com.pinyougou.pojo.TbSeckillGoods;

import java.util.List;
import java.util.Map;

/**
 * 业务逻辑接口
 * @author Steven
 *
 */
public interface SeckillGoodsService {

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbSeckillGoods> findAll();
	
	
	/**
     * 分页查询列表
     * @return
     */
    public PageResult<TbSeckillGoods> findPage(int pageNum, int pageSize, TbSeckillGoods seckillGoods);


	/**
	 * 增加
	*/
	public void add(TbSeckillGoods seckillGoods);


	/**
	 * 修改
	 */
	public void update(TbSeckillGoods seckillGoods);


	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public TbSeckillGoods getById(Long id);


	/**
	 * 批量删除
	 * @param ids
	 */
	public void delete(Long[] ids);

	/**
	 * 生成商品详细页
	 * @param goodsId
	 */
	//public boolean getItemHtml(Long goodsId);

	/**
	 * 跟据id列表，更新状态
	 * @param ids
	 * @param status
	 */
	public void updateStatus(Long[] ids,String status);

	/**
	 * 查询当前正在参与秒杀的商品
	 * @return
	 */
	public List<TbSeckillGoods> findList();

	/**
	 * 跟据秒杀商品的Id获取时间差和库存信息
	 * @param goodsId
	 * @return
	 */
	public Map<String,Object> getGoodsInfoById(Long goodsId);

}
