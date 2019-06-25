package com.pinyougou.entity;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果封装对象
 * @param <T>
 */
public class PageResult<T> implements Serializable {
    private Integer pages;//总页数
    private List<T> list;//分页结果集

    public PageResult() {
    }

    public PageResult(Integer pages, List<T> list) {
        this.pages = pages;
        this.list = list;
    }

    public Integer getPages() {
        return pages;
    }

    public void setPages(Integer pages) {
        this.pages = pages;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    @Override
    public String toString() {
        return "PageResult{" +
                "pages=" + pages +
                ", list=" + list +
                '}';
    }
}
