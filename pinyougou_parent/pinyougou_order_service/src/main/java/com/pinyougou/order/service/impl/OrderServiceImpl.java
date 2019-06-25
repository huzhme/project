package com.pinyougou.order.service.impl;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.pinyougou.common.utils.IdWorker;
import com.pinyougou.entity.Cart;
import com.pinyougou.mapper.TbOrderItemMapper;
import com.pinyougou.mapper.TbPayLogMapper;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.TbPayLog;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.abel533.entity.Example;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbOrderMapper;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 业务逻辑实现
 * @author Steven
 *
 */
@Service
public class OrderServiceImpl implements OrderService {

	@Autowired
	private TbOrderMapper orderMapper;

	@Autowired
	private TbOrderItemMapper orderItemMapper;

	@Autowired
	private IdWorker idWorker; //注入雪花算法

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private TbPayLogMapper payLogMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbOrder> findAll() {
		return orderMapper.select(null);
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbOrder order) {
		//1.从redis中查询出购物车数据
		List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());

		//订单ID列表[3123123,2312312312,12312312]
		List<String> orderIdList = new ArrayList<>();
		//总金额(元)
		double total_fee = 0.0;

		//循环读取购物车数据，保存订单
		for (Cart cart : cartList) {
			//构建订单对象
			long orderId = idWorker.nextId();
			TbOrder tbOrder = new TbOrder();
			tbOrder.setOrderId(orderId);//订单ID
			tbOrder.setPaymentType(order.getPaymentType());// '支付类型，1、在线支付，2、货到付款',
			tbOrder.setStatus("1"); // '状态：1、未付款，2、已付款，3、未发货，4、已发货，5、交易成功，6、交易关闭,7、待评价',
			tbOrder.setCreateTime(new Date());//'订单创建时间',
			tbOrder.setUpdateTime(order.getCreateTime());//'订单更新时间',
			tbOrder.setUserId(order.getUserId());//'用户id',
			tbOrder.setReceiverAreaName(order.getReceiverAreaName());// '收货人地区名称(省，市，县)街道',
			tbOrder.setReceiverMobile(order.getReceiverMobile());// '收货人手机',
			tbOrder.setReceiver(order.getReceiver());// '收货人',
			tbOrder.setSourceType(order.getSourceType());// '订单来源：1:app端，2：pc端，3：M端，4：微信端，5：手机qq端',
			tbOrder.setSellerId(cart.getSellerId());// '商家ID',
			//循环购物车明细
			double money = 0;//统计购物车明细总金额
			for (TbOrderItem orderItem : cart.getOrderItemList()) {
				orderItem.setId(idWorker.nextId());
				orderItem.setOrderId(orderId);
				money +=orderItem.getTotalFee().doubleValue();//计算总金额
				orderItemMapper.insertSelective(orderItem);
			}
			tbOrder.setPayment(new BigDecimal(money));
			orderMapper.insertSelective(tbOrder);
			//添加订单到集合,用于保存日志
			orderIdList.add(orderId+"");
			//添加总金额,用于保存日志
			total_fee += money;
		}
		//如果是微信支付,才保存日志
		if ("1".equals(order.getPaymentType())){
			TbPayLog payLog = new TbPayLog();
			payLog.setOutTradeNo(idWorker.nextId()+"");//支付订单号
			payLog.setCreateTime(new Date());//创建时间
			//处理订单号列表
			String ids = orderIdList.toString().replace("[", "").replace(" ", "").replace("]", "");
			payLog.setOrderList(ids);//订单号列表
			payLog.setPayType("1");//支付类型 1.微信
			payLog.setTotalFee((long) (total_fee * 100));//总金额(分
			payLog.setTradeState("0");//交易状态 0:未支付
			payLog.setUserId(order.getUserId());//用户ID
			//保存到数据库日志表
			payLogMapper.insertSelective(payLog);
			//将payLog放入redis缓存
			redisTemplate.boundHashOps("payLogs").put(order.getUserId(), payLog);
		}

		//清除redis中当前用户的购物车数据
		redisTemplate.boundHashOps("cartList").delete(order.getUserId());
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize,TbOrder order) {
		PageResult<TbOrder> result = new PageResult<TbOrder>();
        //设置分页条件
        PageHelper.startPage(pageNum, pageSize);

        //构建查询条件
        Example example = new Example(TbOrder.class);
        Example.Criteria criteria = example.createCriteria();
		
		if(order!=null){			
						//如果字段不为空
			if (order.getPaymentType()!=null && order.getPaymentType().length()>0) {
				criteria.andLike("paymentType", "%" + order.getPaymentType() + "%");
			}
			//如果字段不为空
			if (order.getPostFee()!=null && order.getPostFee().length()>0) {
				criteria.andLike("postFee", "%" + order.getPostFee() + "%");
			}
			//如果字段不为空
			if (order.getStatus()!=null && order.getStatus().length()>0) {
				criteria.andLike("status", "%" + order.getStatus() + "%");
			}
			//如果字段不为空
			if (order.getShippingName()!=null && order.getShippingName().length()>0) {
				criteria.andLike("shippingName", "%" + order.getShippingName() + "%");
			}
			//如果字段不为空
			if (order.getShippingCode()!=null && order.getShippingCode().length()>0) {
				criteria.andLike("shippingCode", "%" + order.getShippingCode() + "%");
			}
			//如果字段不为空
			if (order.getUserId()!=null && order.getUserId().length()>0) {
				criteria.andLike("userId", "%" + order.getUserId() + "%");
			}
			//如果字段不为空
			if (order.getBuyerMessage()!=null && order.getBuyerMessage().length()>0) {
				criteria.andLike("buyerMessage", "%" + order.getBuyerMessage() + "%");
			}
			//如果字段不为空
			if (order.getBuyerNick()!=null && order.getBuyerNick().length()>0) {
				criteria.andLike("buyerNick", "%" + order.getBuyerNick() + "%");
			}
			//如果字段不为空
			if (order.getBuyerRate()!=null && order.getBuyerRate().length()>0) {
				criteria.andLike("buyerRate", "%" + order.getBuyerRate() + "%");
			}
			//如果字段不为空
			if (order.getReceiverAreaName()!=null && order.getReceiverAreaName().length()>0) {
				criteria.andLike("receiverAreaName", "%" + order.getReceiverAreaName() + "%");
			}
			//如果字段不为空
			if (order.getReceiverMobile()!=null && order.getReceiverMobile().length()>0) {
				criteria.andLike("receiverMobile", "%" + order.getReceiverMobile() + "%");
			}
			//如果字段不为空
			if (order.getReceiverZipCode()!=null && order.getReceiverZipCode().length()>0) {
				criteria.andLike("receiverZipCode", "%" + order.getReceiverZipCode() + "%");
			}
			//如果字段不为空
			if (order.getReceiver()!=null && order.getReceiver().length()>0) {
				criteria.andLike("receiver", "%" + order.getReceiver() + "%");
			}
			//如果字段不为空
			if (order.getInvoiceType()!=null && order.getInvoiceType().length()>0) {
				criteria.andLike("invoiceType", "%" + order.getInvoiceType() + "%");
			}
			//如果字段不为空
			if (order.getSourceType()!=null && order.getSourceType().length()>0) {
				criteria.andLike("sourceType", "%" + order.getSourceType() + "%");
			}
			//如果字段不为空
			if (order.getSellerId()!=null && order.getSellerId().length()>0) {
				criteria.andLike("sellerId", "%" + order.getSellerId() + "%");
			}
	
		}

        //查询数据
        List<TbOrder> list = orderMapper.selectByExample(example);
        //返回数据列表
        result.setList(list);

        //获取总页数
        PageInfo<TbOrder> info = new PageInfo<TbOrder>(list);
        result.setPages(info.getPages());
		
		return result;
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbOrder order){
		orderMapper.updateByPrimaryKeySelective(order);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbOrder getById(Long id){
		return orderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		//数组转list
        List longs = Arrays.asList(ids);
        //构建查询条件
        Example example = new Example(TbOrder.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", longs);

        //跟据查询条件删除数据
        orderMapper.deleteByExample(example);
	}

	/**
	 * 跟据用户Id查找用户订单支付日志
	 * @param userId
	 * @return
	 */
	@Override
	public TbPayLog searchPayLogFromRedis(String userId) {
		return (TbPayLog) redisTemplate.boundHashOps("payLogs").get(userId);
	}

	/**
	 * 跟据订单号修改日志信息和关联的订单状态
	 * @param out_trade_no 订单ID
	 * @param transaction_id 微信返回的交易流水账号
	 */
	@Override
	public void updateOrderStatus(String out_trade_no, String transaction_id) {
		//跟据订单号查询当前下单的订单信息
		TbPayLog payLog = payLogMapper.selectByPrimaryKey(out_trade_no);
		//1. 修改支付日志状态
		payLog.setTransactionId(transaction_id);//设置交易流水账号
		payLog.setPayTime(new Date());//设置支付的时间
		payLog.setTradeState("1");//设置交易状态 1:已支付
		//提交修改
		payLogMapper.updateByPrimaryKeySelective(payLog);

		//2. 修改关联的订单的状态
		String orderList = payLog.getOrderList();//获取订单列表
		String[] orderIds = orderList.split(",");
		for (String orderId : orderIds) {
			TbOrder tbOrder = new TbOrder();
			tbOrder.setUpdateTime(new Date());//订单更新的时间
			tbOrder.setOrderId(new Long(orderId));//订单ID
			tbOrder.setStatus("2");//状态:已支付
			orderMapper.updateByPrimaryKeySelective(tbOrder);
		}
		//3. 清除缓存中的支付日志对象
		redisTemplate.boundHashOps("payLogs").delete(payLog.getUserId());
	}


}
