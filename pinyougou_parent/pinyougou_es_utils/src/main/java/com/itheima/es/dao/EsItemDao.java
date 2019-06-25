package com.itheima.es.dao;

import com.pinyougou.entity.EsItem;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface EsItemDao extends ElasticsearchRepository<EsItem,Long> {
    /**
     * 删除索引
     * @param ids
     */
    void deleteByGoodsIdIn(Long[] ids);
}
