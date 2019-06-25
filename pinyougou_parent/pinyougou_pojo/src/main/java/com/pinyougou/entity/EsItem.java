package com.pinyougou.entity;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import javax.persistence.Id;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * 商品信息es业务实体
 */
@Document(indexName = "pinyougou",type = "item")
public class EsItem implements Serializable {
    /**
     * 商品id，同时也是商品编号
     */
    @Id
    @Field(index = true,store = true,type = FieldType.Long)
    private Long id;

    /**
     * 商品标题
     */
    @Field(index = true,analyzer = "ik_smart",store = true,searchAnalyzer = "ik_smart",type = FieldType.Text)
    private String title;

    /**
     * 商品卖点
     */
    @Field(index = true,analyzer = "ik_smart",store = true,searchAnalyzer = "ik_smart"
            ,type = FieldType.Text,copyTo = "keyword")
    private String sellPoint;

    /**
     * 商品价格，单位为：元
     */
    @Field(store = true,type = FieldType.Double)
    private Double price;

    /**
     * 库存数量
     */
    @Field(store = true,type = FieldType.Integer)
    private Integer num;

    /**
     * 商品图片
     */
    @Field(store = true,type = FieldType.Text)
    private String image;

    /**
     * 更新时间
     */
    @Field(store = true,type = FieldType.Date)
    private Date updateTime;

    @Field(store = true,type = FieldType.Long)
    private Long goodsId;

    @Field(store = true,type = FieldType.Keyword,copyTo = "keyword")
    private String category;

    @Field(store = true,type = FieldType.Keyword,copyTo = "keyword")
    private String brand;

    //嵌套域-用于存储规格
    @Field(store = true,type = FieldType.Nested)
    private Map<String,String> spec;

    @Field(store = true,type = FieldType.Keyword,copyTo = "keyword")
    private String seller;


    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSellPoint() {
        return sellPoint;
    }

    public void setSellPoint(String sellPoint) {
        this.sellPoint = sellPoint;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Long goodsId) {
        this.goodsId = goodsId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public Map<String, String> getSpec() {
        return spec;
    }

    public void setSpec(Map<String, String> spec) {
        this.spec = spec;
    }

    public String getSeller() {
        return seller;
    }

    public void setSeller(String seller) {
        this.seller = seller;
    }

    @Override
    public String toString() {
        return "EsItem{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", sellPoint='" + sellPoint + '\'' +
                ", price=" + price +
                ", num=" + num +
                ", image='" + image + '\'' +
                ", goodsId=" + goodsId +
                ", category='" + category + '\'' +
                ", brand='" + brand + '\'' +
                ", spec=" + spec +
                ", seller='" + seller + '\'' +
                '}';
    }
}