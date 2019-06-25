package com.pinyougou.es.dao;

import com.pinyougou.entity.EsItem;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * 商品信息ES操作对象
 * @author Steven
 * @version 1.0
 * @description com.itheima.es.dao
 * @date 2019-5-31
 */
public interface EsItemDao extends ElasticsearchRepository<EsItem,Long> {
    /**
     * 删除索引
     * @param ids
     */
    void deleteByGoodsIdIn(Long[] ids);
}
