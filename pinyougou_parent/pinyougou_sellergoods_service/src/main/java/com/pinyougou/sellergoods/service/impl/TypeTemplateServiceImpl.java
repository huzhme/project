package com.pinyougou.sellergoods.service.impl;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbSpecificationOptionMapper;
import com.pinyougou.pojo.TbSpecificationOption;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.abel533.entity.Example;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbTypeTemplateMapper;
import com.pinyougou.pojo.TbTypeTemplate;
import com.pinyougou.service.TypeTemplateService;
import com.pinyougou.entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 业务逻辑实现
 * @author Steven
 *
 */
@Service
public class TypeTemplateServiceImpl implements TypeTemplateService {

	@Autowired
	private TbTypeTemplateMapper typeTemplateMapper;

	@Autowired
	private TbSpecificationOptionMapper specificationOptionMapper;

	@Autowired
	private RedisTemplate redisTemplate;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbTypeTemplate> findAll() {
		return typeTemplateMapper.select(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize,TbTypeTemplate typeTemplate) {
		PageResult<TbTypeTemplate> result = new PageResult<TbTypeTemplate>();
        //设置分页条件
        PageHelper.startPage(pageNum, pageSize);

        //构建查询条件
        Example example = new Example(TbTypeTemplate.class);
        Example.Criteria criteria = example.createCriteria();
		
		if(typeTemplate!=null){			
						//如果字段不为空
			if (typeTemplate.getName()!=null && typeTemplate.getName().length()>0) {
				criteria.andLike("name", "%" + typeTemplate.getName() + "%");
			}
			//如果字段不为空
			if (typeTemplate.getSpecIds()!=null && typeTemplate.getSpecIds().length()>0) {
				criteria.andLike("specIds", "%" + typeTemplate.getSpecIds() + "%");
			}
			//如果字段不为空
			if (typeTemplate.getBrandIds()!=null && typeTemplate.getBrandIds().length()>0) {
				criteria.andLike("brandIds", "%" + typeTemplate.getBrandIds() + "%");
			}
			//如果字段不为空
			if (typeTemplate.getCustomAttributeItems()!=null && typeTemplate.getCustomAttributeItems().length()>0) {
				criteria.andLike("customAttributeItems", "%" + typeTemplate.getCustomAttributeItems() + "%");
			}
	
		}

        //查询数据
        List<TbTypeTemplate> list = typeTemplateMapper.selectByExample(example);
        //返回数据列表
        result.setList(list);

        //获取总页数
        PageInfo<TbTypeTemplate> info = new PageInfo<TbTypeTemplate>(list);
        result.setPages(info.getPages());

		//存入redis缓存
        saveToRedis();

		return result;
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbTypeTemplate typeTemplate) {
		typeTemplateMapper.insertSelective(typeTemplate);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbTypeTemplate typeTemplate){
		typeTemplateMapper.updateByPrimaryKeySelective(typeTemplate);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbTypeTemplate getById(Long id){
		return typeTemplateMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		//数组转list
        List longs = Arrays.asList(ids);
        //构建查询条件
        Example example = new Example(TbTypeTemplate.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", longs);

        //跟据查询条件删除数据
        typeTemplateMapper.deleteByExample(example);
	}

	/**
	 * 根据模板id查找规格列表
	 * @param templateId
	 * @return
	 */
    @Override
    public List<Map> findSpecList(Long templateId) {
    	//查询模板id对应模板信息
		TbTypeTemplate typeTemplate = typeTemplateMapper.selectByPrimaryKey(templateId);

		//把json串转成List<Map>
		List<Map> list = JSON.parseArray(typeTemplate.getSpecIds(), Map.class);

		TbSpecificationOption specificationOption = null;
		//遍历规格列表，查询规格选项列表
		for (Map map : list) {
			//构建查询条件,跟据规格id查询规格选项列表
			specificationOption = new TbSpecificationOption();
			specificationOption.setSpecId(new Long(map.get("id").toString()));
			List<TbSpecificationOption> optionList = specificationOptionMapper.select(specificationOption);
			map.put("optionList", optionList);
		}
		System.out.println(list);
		return list;
    }

	/**
	 * 将数据放入缓存redis中
	 */
    private void saveToRedis(){
		//分别将品牌数据和规格数据放入缓存（Hash）。以模板ID作为key,以品牌列表和规格列表作为值。
		List<TbTypeTemplate> typeTemplateList = findAll();
		for (TbTypeTemplate typeTemplate : typeTemplateList) {
			//缓存品牌列表
			List<Map> brandList = JSON.parseArray(typeTemplate.getBrandIds(), Map.class);
			redisTemplate.boundHashOps("brandList").put(typeTemplate.getId(), brandList);

			//缓存规格列表
			List<Map> specList = findSpecList(typeTemplate.getId());
			redisTemplate.boundHashOps("specList").put(typeTemplate.getId(), specList);
		}
	}

}
