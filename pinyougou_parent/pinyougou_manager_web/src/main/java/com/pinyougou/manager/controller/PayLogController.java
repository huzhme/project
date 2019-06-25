package com.pinyougou.manager.controller;
import java.util.List;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.service.PayLogService;

import com.pinyougou.entity.PageResult;
import com.pinyougou.entity.ResultInfo;
/**
 * 请求处理器
 * @author Steven
 *
 */
@RestController
@RequestMapping("/payLog")
public class PayLogController {

	@Reference
	private PayLogService payLogService;
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbPayLog> findAll(){			
		return payLogService.findAll();
	}
	
	
	/**
	 * 分页查询数据
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult  findPage(int pageNo,int pageSize,@RequestBody TbPayLog payLog){			
		return payLogService.findPage(pageNo, pageSize,payLog);
	}
	
	/**
	 * 增加
	 * @param payLog
	 * @return
	 */
	@RequestMapping("/add")
	public ResultInfo add(@RequestBody TbPayLog payLog){
		try {
			payLogService.add(payLog);
			return new ResultInfo(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new ResultInfo(false, "增加失败");
		}
	}
	
	/**
	 * 修改
	 * @param payLog
	 * @return
	 */
	@RequestMapping("/update")
	public ResultInfo update(@RequestBody TbPayLog payLog){
		try {
			payLogService.update(payLog);
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
	public TbPayLog getById(Long id){
		return payLogService.getById(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public ResultInfo delete(Long [] ids){
		try {
			payLogService.delete(ids);
			return new ResultInfo(true, "删除成功"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new ResultInfo(false, "删除失败");
		}
	}
	
}
