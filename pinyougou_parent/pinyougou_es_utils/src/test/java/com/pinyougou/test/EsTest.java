package com.pinyougou.test;

import com.alibaba.fastjson.JSON;
import com.itheima.es.dao.EsItemDao;
import com.pinyougou.entity.EsItem;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:spring/applicationContext*.xml")
public class EsTest {

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private EsItemDao esItemDao;

    @Test
    public void testAdd(){
        //只导入已审核的商品
        TbItem tbItem = new TbItem();
        tbItem.setStatus("1");
        List<TbItem> itemList = itemMapper.select(tbItem);
        System.out.println("总共将要导入 " + itemList.size() + " 个商品。");

        //es数据实体列表
        List<EsItem> esItemList = new ArrayList<>();
        EsItem esItem = null;
        for (TbItem item : itemList) {
            esItem = new EsItem();
            //使用spring的BeanUtils深克隆对象
            //相当于把TbItem所有属性名与数据类型相同的属性值设置到EsItem中
            BeanUtils.copyProperties(item, esItem);
            //注意，这里价格的类型不一样需要单独设置
            esItem.setPrice(item.getPrice().doubleValue());
            //嵌套域-规格数据绑定
            Map specMap = JSON.parseObject(item.getSpec(), Map.class);
            esItem.setSpec(specMap);
            //组装es实体列表
            esItemList.add(esItem);
        }
        System.out.println("开始导入到索引库...");
        esItemDao.saveAll(esItemList);
        System.out.println("索引库导入完毕...");
    }

    @Test
    public void test02(){
        Long[] ids = {149187842867955L, 1L, 149187842867960L, 149187842867973L};
        esItemDao.deleteByGoodsIdIn(ids);
    }
}
