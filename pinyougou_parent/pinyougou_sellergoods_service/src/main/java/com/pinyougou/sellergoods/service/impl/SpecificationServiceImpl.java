package com.pinyougou.sellergoods.service.impl;
import java.util.Arrays;
import java.util.List;

import com.pinyougou.mapper.TbSpecificationOptionMapper;
import com.pinyougou.pojo.TbSpecificationOption;
import com.pinyougou.pojogroup.Specification;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.abel533.entity.Example;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSpecificationMapper;
import com.pinyougou.pojo.TbSpecification;
import com.pinyougou.service.SpecificationService;
import com.pinyougou.entity.PageResult;

/**
 * 业务逻辑实现
 * @author Steven
 *
 */
@Service
public class SpecificationServiceImpl implements SpecificationService {

	@Autowired
	private TbSpecificationMapper specificationMapper;

	@Autowired
	private TbSpecificationOptionMapper specificationOptionMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSpecification> findAll() {
		return specificationMapper.select(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize,TbSpecification specification) {
		PageResult<TbSpecification> result = new PageResult<TbSpecification>();
        //设置分页条件
        PageHelper.startPage(pageNum, pageSize);

        //构建查询条件
        Example example = new Example(TbSpecification.class);
        Example.Criteria criteria = example.createCriteria();
		
		if(specification!=null){			
						//如果字段不为空
			if (specification.getSpecName()!=null && specification.getSpecName().length()>0) {
				criteria.andLike("specName", "%" + specification.getSpecName() + "%");
			}
	
		}

        //查询数据
        List<TbSpecification> list = specificationMapper.selectByExample(example);
        //返回数据列表
        result.setList(list);

        //获取总页数
        PageInfo<TbSpecification> info = new PageInfo<TbSpecification>(list);
        result.setPages(info.getPages());
		
		return result;
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(Specification specification){
		//更新规格信息
		specificationMapper.updateByPrimaryKeySelective(specification.getSpecification());

		//更新规格选项信息
		//先删除规格id绑定的选项信息，再添加
		Example example = new Example(TbSpecificationOption.class);
		Example.Criteria criteria = example.createCriteria();
		criteria.andEqualTo("specId", specification.getSpecification().getId());
		specificationOptionMapper.deleteByExample(example);

		//再添加规格id
		for (TbSpecificationOption option : specification.getSpecificationOptionList()) {
			option.setSpecId(specification.getSpecification().getId());
			specificationOptionMapper.insertSelective(option);
		}
	}
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Specification getById(Long id){
		//返回的Specification规格，规格选项信息
		Specification specification = new Specification();

		//获取规格信息
		TbSpecification tbSpecification = specificationMapper.selectByPrimaryKey(id);
		specification.setSpecification(tbSpecification);

		//获取规格信息
		TbSpecificationOption where = new TbSpecificationOption();
		where.setSpecId(id);
		List<TbSpecificationOption> tbSpecificationOptionList = specificationOptionMapper.select(where);
		specification.setSpecificationOptionList(tbSpecificationOptionList);

		return specification;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		//数组转list
        List longs = Arrays.asList(ids);
        //构建查询条件
        Example example = new Example(TbSpecification.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", longs);

        //跟据查询条件删除数据
        specificationMapper.deleteByExample(example);

        //删除规格选项数据
		//构建查询条件
		Example example2 = new Example(TbSpecificationOption.class);
		Example.Criteria criteria2 = example2.createCriteria();
		criteria2.andIn("specId", longs);
		specificationOptionMapper.deleteByExample(example2);
	}

	/**
	 * 增加
	 * @param specification
	 */
    @Override
    public void add(Specification specification) {
    	//保存规格
        specificationMapper.insertSelective(specification.getSpecification());

        //保存规格选项
		for (TbSpecificationOption option : specification.getSpecificationOptionList()) {
			//设置保存规格ID
			option.setSpecId(specification.getSpecification().getId());
			specificationOptionMapper.insertSelective(option);
		}
    }


}
