package com.pinyougou.seckill.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.abel533.entity.Example;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.entity.PageResult;
import com.pinyougou.entity.QueueTag;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.mapper.TbSeckillOrderMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 业务逻辑实现
 * @author Steven
 *
 */
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

	@Autowired
	private TbSeckillOrderMapper seckillOrderMapper;

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private MultiThreadWork multiThreadWork;

	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;


	/**
	 * 查询全部
	 */
	@Override
	public List<TbSeckillOrder> findAll() {
		return seckillOrderMapper.select(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize,TbSeckillOrder seckillOrder) {
		PageResult<TbSeckillOrder> result = new PageResult<TbSeckillOrder>();
        //设置分页条件
        PageHelper.startPage(pageNum, pageSize);

        //构建查询条件
        Example example = new Example(TbSeckillOrder.class);
        Example.Criteria criteria = example.createCriteria();
		
		if(seckillOrder!=null){			
						//如果字段不为空
			if (seckillOrder.getUserId()!=null && seckillOrder.getUserId().length()>0) {
				criteria.andLike("userId", "%" + seckillOrder.getUserId() + "%");
			}
			//如果字段不为空
			if (seckillOrder.getSellerId()!=null && seckillOrder.getSellerId().length()>0) {
				criteria.andLike("sellerId", "%" + seckillOrder.getSellerId() + "%");
			}
			//如果字段不为空
			if (seckillOrder.getStatus()!=null && seckillOrder.getStatus().length()>0) {
				criteria.andLike("status", "%" + seckillOrder.getStatus() + "%");
			}
			//如果字段不为空
			if (seckillOrder.getReceiverAddress()!=null && seckillOrder.getReceiverAddress().length()>0) {
				criteria.andLike("receiverAddress", "%" + seckillOrder.getReceiverAddress() + "%");
			}
			//如果字段不为空
			if (seckillOrder.getReceiverMobile()!=null && seckillOrder.getReceiverMobile().length()>0) {
				criteria.andLike("receiverMobile", "%" + seckillOrder.getReceiverMobile() + "%");
			}
			//如果字段不为空
			if (seckillOrder.getReceiver()!=null && seckillOrder.getReceiver().length()>0) {
				criteria.andLike("receiver", "%" + seckillOrder.getReceiver() + "%");
			}
			//如果字段不为空
			if (seckillOrder.getTransactionId()!=null && seckillOrder.getTransactionId().length()>0) {
				criteria.andLike("transactionId", "%" + seckillOrder.getTransactionId() + "%");
			}
	
		}

        //查询数据
        List<TbSeckillOrder> list = seckillOrderMapper.selectByExample(example);
        //返回数据列表
        result.setList(list);

        //获取总页数
        PageInfo<TbSeckillOrder> info = new PageInfo<TbSeckillOrder>(list);
        result.setPages(info.getPages());
		
		return result;
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSeckillOrder seckillOrder) {
		seckillOrderMapper.insertSelective(seckillOrder);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbSeckillOrder seckillOrder){
		seckillOrderMapper.updateByPrimaryKeySelective(seckillOrder);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbSeckillOrder getById(Long id){
		return seckillOrderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		//数组转list
        List longs = Arrays.asList(ids);
        //构建查询条件
        Example example = new Example(TbSeckillOrder.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", longs);

        //跟据查询条件删除数据
        seckillOrderMapper.deleteByExample(example);
	}

	/**
	 * 立即抢购
	 * @param seckillId 抢购商品ID
	 * @param userId	用户ID
	 */
	@Override
	public void submitOrder(Long seckillId, String userId) {
		//解决抢单`重复下单问题
		QueueTag queueTag = (QueueTag) redisTemplate.boundHashOps("user_order_info_" + userId).get(seckillId);
		//如果用户的订单信息存在未支付或者正在排队状态
		if (queueTag == QueueTag.IN_LINE || queueTag == QueueTag.CREATE_ORDER){
			throw new RuntimeException("当前商品，您已存在未付款订单，请先支付！");
		}

		//登记排队信息,(每个商品单独排一队),左进右取,以商品的id为key,用户id为value
		redisTemplate.boundListOps("seckill_goods_order_queue_"+seckillId).leftPush(userId);
		//记录用户已在排队的信息,redis中key=用户id {商品id:排队标识}
		redisTemplate.boundHashOps("user_order_info_"+userId).put(seckillId, QueueTag.IN_LINE);

		//开启多线程下单
		multiThreadWork.createOrder(seckillId);
	}

	/**
	 * 跟据用户ID和商品ID查询排队标识状态
	 * @param userId
	 * @param seckillId
	 * @return
	 */
	@Override
	public QueueTag getQueueStatus(String userId, Long seckillId) {
		QueueTag queueStatus = (QueueTag) redisTemplate.boundHashOps("user_order_info_" + userId).get(seckillId);
		return queueStatus;
	}

	/**
	 * 跟据用户名查询秒杀订单
	 * @param userId 用户名
	 * @return
	 */
	@Override
	public TbSeckillOrder searchOrderFromRedisByUserId(String userId) {
		return (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
	}

	/**
	 * 跟据用户名获取秒杀订单信息保存到数据库
	 * @param userId
	 * @param transaction_id
	 */
	@Override
	public void saveOrderFromRedisToDb(String userId, String transaction_id) {
		TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
		if (seckillOrder == null){
		    throw new RuntimeException("订单不存在");
		}
		seckillOrder.setTransactionId(transaction_id);//交易流水号
		seckillOrder.setPayTime(new Date());//支付时间
		seckillOrder.setStatus("1");//状态
		//保存到数据库
		seckillOrderMapper.insertSelective(seckillOrder);
		//从redis中删除秒杀订单信息
		redisTemplate.boundHashOps("seckillOrder").delete(userId);
		//记录用户标识信息为已支付
		redisTemplate.boundHashOps("user_order_info_"+userId).put(seckillOrder.getSeckillId(), QueueTag.PAY_SUCCESS);
	}

	/**
	 * 从缓存中删除订单并还原库存(用于订单超时)
	 * @param userId
	 * @param orderId
	 */
	@Override
	public void deleteOrderFromRedis(String userId, Long orderId) {
		//从redis中获取下单信息
		TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
		if (seckillOrder != null){
		    //删除redis中的订单信息
			redisTemplate.boundHashOps("seckillOrder").delete(userId);
			//还原库存
			//从缓存中读入秒杀商品信息
			TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillOrder.getSeckillId());
			if (seckillGoods == null){
				//操作数据库数据
				seckillGoods = seckillGoodsMapper.selectByPrimaryKey(seckillOrder.getSeckillId());
			}
			//操作redis缓存数据
			seckillGoods.setStockCount(seckillGoods.getStockCount()+1);
			//恢复库存-页面展示
			redisTemplate.boundHashOps("seckillGoods").put(seckillOrder.getSeckillId(), seckillGoods);
			//恢复redis中的库存-下单时用的
			redisTemplate.boundHashOps("seckillStockCount").increment(seckillOrder.getSeckillId(), 1);
		}
	}
}
