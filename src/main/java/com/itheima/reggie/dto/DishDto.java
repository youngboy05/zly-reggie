package com.itheima.reggie.dto;

import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/*
 * DTO: data transfer object,即数据传输对象，一般用于展示层与服务层之间的数据传输
 * 也就是说，当前端的操作传输的数据涉及到多张表时，由于实体类的字段无法满足接收需求，所以需要另外定义一个类来封装这些数据；
 */
@Data
public class DishDto extends Dish {

    private List<DishFlavor> flavors = new ArrayList<>();

    private String categoryName;

    private Integer copies;

}