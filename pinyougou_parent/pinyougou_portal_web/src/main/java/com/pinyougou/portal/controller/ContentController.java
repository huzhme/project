package com.pinyougou.portal.controller;
import java.util.List;

import com.pinyougou.content.service.ContentService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbContent;

import com.pinyougou.entity.PageResult;
import com.pinyougou.entity.ResultInfo;
/**
 * 请求处理器
 * @author xiaobo
 *
 */
@RestController
@RequestMapping("/content")
public class ContentController {

	@Reference
	private ContentService contentService;
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbContent> findAll(){			
		return contentService.findAll();
	}
	
	
	/**
	 * 分页查询数据
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult  findPage(int pageNo,int pageSize,@RequestBody TbContent content){			
		return contentService.findPage(pageNo, pageSize,content);
	}
	
	/**
	 * 增加
	 * @param content
	 * @return
	 */
	@RequestMapping("/add")
	public ResultInfo add(@RequestBody TbContent content){
		try {
			contentService.add(content);
			return new ResultInfo(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new ResultInfo(false, "增加失败");
		}
	}
	
	/**
	 * 修改
	 * @param content
	 * @return
	 */
	@RequestMapping("/update")
	public ResultInfo update(@RequestBody TbContent content){
		try {
			contentService.update(content);
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
	public TbContent getById(Long id){
		return contentService.getById(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public ResultInfo delete(Long [] ids){
		try {
			contentService.delete(ids);
			return new ResultInfo(true, "删除成功"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new ResultInfo(false, "删除失败");
		}
	}

	/**
	 * 根据广告分类ID查询广告列表
	 * @param categoryId
	 * @return
	 */
	@RequestMapping("/findByCategoryId")
	public List<TbContent> findByCategoryId(Long categoryId){
		return contentService.findByCategoryId(categoryId);
	}
	
}
