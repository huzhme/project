package com.pinyougou.seckill.service.impl;

import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xiaobo
 * @package com.pinyougou.seckill.service.impl
 * @time 2019/6/16 0016 17:25
 * @Version: 1.0
 */
@Component
public class CreatePageService {

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;

    @Value("${pagedir}")
    private String pagedir;

    /*****
     * 秒杀商品ID  此处建议使用多线程生成文件
     * @param seckillGoodsId
     * @return
     */
    public boolean buildHtml(Long seckillGoodsId){
        try {
            //读取模板对象
            Configuration cfg = freeMarkerConfigurer.getConfiguration();
            Template template = cfg.getTemplate("seckill-item.ftl");

            //构建数据模型对象
            Map<String,Object> map = new HashMap();

            //查询秒杀商品信息
            TbSeckillGoods tbSeckillGoods = seckillGoodsMapper.selectByPrimaryKey(seckillGoodsId);
            map.put("seckillGoods", tbSeckillGoods);

            //输出静态页面
            Writer out = new FileWriter(pagedir+"seckill-" + seckillGoodsId + ".html");
            template.process(map, out);
            //关闭资源
            out.close();
            System.out.println("生成了商品详情页：" + seckillGoodsId);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
