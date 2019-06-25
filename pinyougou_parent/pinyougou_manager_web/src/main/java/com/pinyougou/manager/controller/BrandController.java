package com.pinyougou.manager.controller;
import java.util.List;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.service.BrandService;

import com.pinyougou.entity.PageResult;
import com.pinyougou.entity.ResultInfo;
/**
 * 请求处理器
 * @author Steven
 *
 */
@RestController
@RequestMapping("/brand")
public class BrandController {

	@Reference
	private BrandService brandService;
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbBrand> findAll(){			
		return brandService.findAll();
	}
	
	
	/**
	 * 分页查询数据
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult  findPage(int pageNo,int pageSize,@RequestBody TbBrand brand){			
		return brandService.findPage(pageNo, pageSize,brand);
	}
	
	/**
	 * 增加
	 * @param brand
	 * @return
	 */
	@RequestMapping("/add")
	public ResultInfo add(@RequestBody TbBrand brand){
		try {
			brandService.add(brand);
			return new ResultInfo(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new ResultInfo(false, "增加失败");
		}
	}
	
	/**
	 * 修改
	 * @param brand
	 * @return
	 */
	@RequestMapping("/update")
	public ResultInfo update(@RequestBody TbBrand brand){
		try {
			brandService.update(brand);
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
	public TbBrand getById(Long id){
		return brandService.getById(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public ResultInfo delete(Long [] ids){
		try {
			brandService.delete(ids);
			return new ResultInfo(true, "删除成功"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new ResultInfo(false, "删除失败");
		}
	}
	
}
