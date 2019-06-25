package com.pinyougou.sellergoods.service.impl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.abel533.entity.Example;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.service.ItemCatService;
import com.pinyougou.entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 业务逻辑实现
 * @author Steven
 *
 */
@Service
public class ItemCatServiceImpl implements ItemCatService {

	@Autowired
	private TbItemCatMapper itemCatMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbItemCat> findAll() {
		return itemCatMapper.select(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize,TbItemCat itemCat) {
		PageResult<TbItemCat> result = new PageResult<TbItemCat>();
        //设置分页条件
        PageHelper.startPage(pageNum, pageSize);

        //构建查询条件
        Example example = new Example(TbItemCat.class);
        Example.Criteria criteria = example.createCriteria();
		
		if(itemCat!=null){			
						//如果字段不为空
			if (itemCat.getName()!=null && itemCat.getName().length()>0) {
				criteria.andLike("name", "%" + itemCat.getName() + "%");
			}
	
		}

        //查询数据
        List<TbItemCat> list = itemCatMapper.selectByExample(example);
        //返回数据列表
        result.setList(list);

        //获取总页数
        PageInfo<TbItemCat> info = new PageInfo<TbItemCat>(list);
        result.setPages(info.getPages());
		
		return result;
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbItemCat itemCat) {
		itemCatMapper.insertSelective(itemCat);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbItemCat itemCat){
		itemCatMapper.updateByPrimaryKeySelective(itemCat);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbItemCat getById(Long id){
		return itemCatMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		//数组转list
        //List longs = Arrays.asList(ids);

        //定义集合存储需要删除的所有id
        List list = new ArrayList<>();
        for (Long id : ids) {
            getAllIds(list, id);
        }
        //构建查询条件
        Example example = new Example(TbItemCat.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", list);

        //跟据查询条件删除数据
        itemCatMapper.deleteByExample(example);
	}

    /**
     * 根据父id查找下一级的所有id
     * @param list
     * @param id
     */
	public void getAllIds(List list,Long id){
	    //先将当前id保存起来
        list.add(id);
        //查询当前节点的所有子节点
        List<TbItemCat> byParentId = findByParentId(id);
        if (byParentId!=null && byParentId.size()>0){
            for (TbItemCat itemCat : byParentId) {
                //递归查询
                getAllIds(list, itemCat.getId());
            }
        }
    }

    @Autowired
	private RedisTemplate redisTemplate;

	/**
	 * 根据父ID查找商品分类列表
	 * @param parentId
	 * @return
	 */
    @Override
    public List<TbItemCat> findByParentId(Long parentId) {
    	//构建查询条件
    	TbItemCat itemCat = new TbItemCat();
    	itemCat.setParentId(parentId);
    	//查询数据
		List<TbItemCat> itemCats = itemCatMapper.select(itemCat);

		//将商品分类数据放入缓存redis（Hash）。以分类名称作为key ,以模板ID作为值
		//在这里写的原因是商品分类增删改都会经过这个方法
		List<TbItemCat> itemCatList = this.findAll();
		for (TbItemCat tbItemCat : itemCatList) {
			redisTemplate.boundHashOps("itemCat").put(tbItemCat.getName(), tbItemCat.getTypeId());
		}
		return itemCats;
	}


}
