package com.pinyougou.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.search.service.EsItemService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/esItem")
public class EsItemController {

    @Reference
    private EsItemService esItemService;

    @RequestMapping("/search")
    public Map<String,Object> search(@RequestBody Map search){
        return esItemService.search(search);
    }
}
