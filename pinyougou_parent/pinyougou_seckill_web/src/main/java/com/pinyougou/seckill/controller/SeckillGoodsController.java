package com.pinyougou.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.entity.PageResult;
import com.pinyougou.entity.ResultInfo;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.seckill.service.SeckillGoodsService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 请求处理器
 * @author xiaobo
 *
 */
@RestController
@RequestMapping("/seckillGoods")
public class SeckillGoodsController {

	@Reference
	private SeckillGoodsService seckillGoodsService;
	
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
	 * 查询当前正在参与秒杀的商品
	 * @return
	 */
	@RequestMapping("/findList")
	public List<TbSeckillGoods> findList(){
		return seckillGoodsService.findList();
	}


	/**
	 * 跟据秒杀商品的Id获取时间差和库存信息
	 * @param goodsId
	 * @return
	 */
	@RequestMapping("/getGoodsInfoById")
	public Map<String,Object> getGoodsInfoById(Long goodsId){
		return seckillGoodsService.getGoodsInfoById(goodsId);
	}
}
