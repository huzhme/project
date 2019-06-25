package com.pinyougou.sellergoods.service.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import com.pinyougou.pojogroup.Goods;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.abel533.entity.Example;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.PageHelper;
import com.pinyougou.service.GoodsService;
import com.pinyougou.entity.PageResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * 业务逻辑实现
 *
 * @author xiaobo
 */
@Service(interfaceClass = GoodsService.class)
@Transactional
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private TbGoodsMapper goodsMapper;

    @Autowired
    private TbGoodsDescMapper goodsDescMapper;

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private TbItemCatMapper itemCatMapper;

    @Autowired
    private TbSellerMapper sellerMapper;

    @Autowired
    private TbBrandMapper brandMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbGoods> findAll() {
        return goodsMapper.select(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize, TbGoods goods) {
        PageResult<TbGoods> result = new PageResult<TbGoods>();
        //设置分页条件
        PageHelper.startPage(pageNum, pageSize);

        //构建查询条件
        Example example = new Example(TbGoods.class);
        Example.Criteria criteria = example.createCriteria();

        if (goods != null) {
            //如果字段不为空
            if (goods.getSellerId() != null && goods.getSellerId().length() > 0) {
                criteria.andLike("sellerId", "%" + goods.getSellerId() + "%");
            }
            //如果字段不为空
            if (goods.getGoodsName() != null && goods.getGoodsName().length() > 0) {
                criteria.andLike("goodsName", "%" + goods.getGoodsName() + "%");
            }
            //如果字段不为空
            if (goods.getAuditStatus() != null && goods.getAuditStatus().length() > 0) {
                criteria.andLike("auditStatus", "%" + goods.getAuditStatus() + "%");
            }
            //如果字段不为空
            if (goods.getIsMarketable() != null && goods.getIsMarketable().length() > 0) {
                criteria.andLike("isMarketable", "%" + goods.getIsMarketable() + "%");
            }
            //如果字段不为空
            if (goods.getCaption() != null && goods.getCaption().length() > 0) {
                criteria.andLike("caption", "%" + goods.getCaption() + "%");
            }
            //如果字段不为空
            if (goods.getSmallPic() != null && goods.getSmallPic().length() > 0) {
                criteria.andLike("smallPic", "%" + goods.getSmallPic() + "%");
            }
            //如果字段不为空
            if (goods.getIsEnableSpec() != null && goods.getIsEnableSpec().length() > 0) {
                criteria.andLike("isEnableSpec", "%" + goods.getIsEnableSpec() + "%");
            }
            /*//如果字段不为空
            if (goods.getIsDelete() != null && goods.getIsDelete().length() > 0) {
                criteria.andLike("isDelete", "%" + goods.getIsDelete() + "%");
            }*/
            //查询没删除的数据
            criteria.andIsNull("isDelete");

        }

        //查询数据
        List<TbGoods> list = goodsMapper.selectByExample(example);
        //返回数据列表
        result.setList(list);

        //获取总页数
        PageInfo<TbGoods> info = new PageInfo<TbGoods>(list);
        result.setPages(info.getPages());

        return result;
    }

    /**
     * 增加
     */
    @Override
    public void add(Goods goods) {
        //添加商品信息
        goods.getGoods().setAuditStatus("0");
        goodsMapper.insertSelective(goods.getGoods());

        //设置商品id给拓展信息表
        goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());

        //添加商品拓展信息
        goodsDescMapper.insertSelective(goods.getGoodsDesc());
        saveItemList(goods);
    }

    /**
     * 分离添加sku列表基础数据
     * @param goods
     */
    public void saveItemList(Goods goods){
        if ("1".equals(goods.getGoods().getIsEnableSpec())) {
            //保存SKU
            for (TbItem item : goods.getItemList()) {
                //商品标题=spu+sku
                String title = goods.getGoods().getGoodsName();//SPU
                //sku
                Map<String, Object> skuMap = JSON.parseObject(item.getSpec());
                for (String key : skuMap.keySet()) {
                    title = title + " " + skuMap.get(key);
                }
                //设置商品标题
                item.setTitle(title);
                //设置商品sku详细信息
                setItemValue(goods,item);
                //保存sku
                itemMapper.insertSelective(item);
            }
        } else {
            TbItem item = new TbItem();
            //商品标题=spu
            String title = goods.getGoods().getGoodsName();//SPU
            //设置商品标题
            item.setTitle(title);
            item.setPrice( goods.getGoods().getPrice() );//价格
            item.setStatus("1");//状态
            item.setIsDefault("1");//是否默认
            item.setNum(99999);//库存数量
            item.setSpec("{}");
            //设置商品sku详细信息
            setItemValue(goods,item);
            itemMapper.insertSelective(item);
        }
    }

    /**
     * 设置商品详细信息
     *
     * @param goods
     * @param item
     */
    public void setItemValue(Goods goods, TbItem item) {
        //商品图片取spu的第一张
        List<Map> imgList = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
        if (imgList.size() > 0) {
            item.setImage(imgList.get(0).get("url").toString());
        }
        //商品类目id
        item.setCategoryid(goods.getGoods().getCategory3Id());
        //查询商品类目内容
        TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(item.getCategoryid());
        item.setCategory(itemCat.getName());
        //创建日期
        item.setCreateTime(new Date());
        //更新日期
        item.setUpdateTime(item.getCreateTime());
        //所属spu-id
        item.setGoodsId(goods.getGoods().getId());
        //所属商家id
        item.setSellerId(goods.getGoods().getSellerId());
        //所属商家名称
        TbSeller tbSeller = sellerMapper.selectByPrimaryKey(item.getSellerId());
        item.setSeller(tbSeller.getNickName());
        //品牌信息
        TbBrand tbBrand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
        item.setBrand(tbBrand.getName());
    }


    /**
     * 修改
     */
    @Override
    public void update(Goods goods) {
        //修改过的商品，状态设置为未审核，重新审核一次
        goods.getGoods().setAuditStatus("0");
        //更新商品基本信息
        goodsMapper.updateByPrimaryKeySelective(goods.getGoods());
        //更新商品扩展信息
        goodsDescMapper.updateByPrimaryKeySelective(goods.getGoodsDesc());
        //更新sku信息，更新前先删除原来的sku
        TbItem tbItem = new TbItem();
        tbItem.setGoodsId(goods.getGoods().getId());
        itemMapper.delete(tbItem);
        //保存新的SKU
        saveItemList(goods);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public Goods getById(Long id) {
        Goods goods = new Goods();
        //查询商品基本信息
        TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
        goods.setGoods(tbGoods);
        //查询商品扩展信息
        TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(id);
        goods.setGoodsDesc(tbGoodsDesc);
        //查询商品sku列表
        TbItem tbItem = new TbItem();
        tbItem.setGoodsId(id);
        List<TbItem> itemList = itemMapper.select(tbItem);
        goods.setItemList(itemList);
        //返回结果
        return goods;
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        //返回结果
        TbGoods tbGoods = new TbGoods();
        //更改删除状态
        tbGoods.setIsDelete("1");
        //数组转list
        List longs = Arrays.asList(ids);
        //构建查询条件
        Example example = new Example(TbGoods.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", longs);

        //跟据查询条件物理删除数据
        goodsMapper.updateByExampleSelective(tbGoods,example);
    }

    /**
     * 跟据id列表，更新状态
     * @param ids
     * @param status
     */
    @Override
    public void updateStatus(Long[] ids, String status) {
        //要更新的内容
        TbGoods tbGoods = new TbGoods();
        tbGoods.setAuditStatus(status);
        //构建更新条件
        Example example = new Example(TbGoods.class);
        Example.Criteria criteria = example.createCriteria();
        List longs = Arrays.asList(ids);
        criteria.andIn("id", longs);
        //执行带条件的更新
        goodsMapper.updateByExampleSelective(tbGoods, example);
    }

    @Override
    public List<TbItem> findItemListByGoodsIdsAndStatus(Long[] goodsIds, String status) {
        //构建条件
        Example example = new Example(TbItem.class);
        Example.Criteria criteria = example.createCriteria();
        //设置条件 spu列表
        List longs = Arrays.asList(goodsIds);
        criteria.andIn("goodsId", longs);
        //设置条件 商品状态
        criteria.andEqualTo("status", status);
        //查询结果
        List<TbItem> tbItems = itemMapper.selectByExample(example);
        return tbItems;
    }


}
