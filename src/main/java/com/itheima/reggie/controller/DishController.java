package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private CategoryService categoryService;

    /*
     * 菜品管理分页查询：
     *    由于菜品管理分页查询中，菜品分类在前端展示的是 categoryName，而通过查询 dish表只能获得 categoryId，
     *    所以我们需要通过 查询得到 categoryId 再次 查询 category 表来得到 categoryName；
     */
    @GetMapping("/page")
    public R<Page> page(Integer page, Integer pageSize, String name) {
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>(); // DishDto继承自Dish，且新增了属性categoryName，所以完全满足前端所需要相应的数据封装要求

        //先通过分页查询dish表，得到菜品的相关信息
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name != null, Dish::getName, name);
        queryWrapper.orderByAsc(Dish::getCategoryId);
        dishService.page(pageInfo, queryWrapper);

        // 利用对象拷贝，将除 records之外的所有信息都拷贝给 dishDtoPage对象
        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");
        List<Dish> records = pageInfo.getRecords(); // 获取records集合

        // 取出records集合中的 categoryId，并以此查询 categoryName
        List<DishDto> list = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto); // 将相关信息拷贝给dishDto
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId); // 查询categoryName
            dishDto.setCategoryName(category.getName()); // 为DishDto对象的 categoryName属性赋值
            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);
        return R.success(dishDtoPage);
    }

    /*
     * 新增菜品，由于涉及到多表操作，所以需要自行在服务层设计对应的方法
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功！");
    }

    /*
     * 根据id查询并回显菜品信息，同样，涉及到多表操作，需要自行在服务层进行方法扩展
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id) {
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    /*
     * 修改菜品信息，涉及多表操作，需要自行扩展；
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        dishService.updateDishWithFlavor(dishDto);
        return R.success("菜品信息修改成功！");
    }

    /*
     * 删除菜品信息，涉及多表操作，需要自行扩展；
     * 删除逻辑：需要确定当前删除的菜品中是否包含正在出售的菜品，若包含则不能执行删除操作；
     *          其次，需要确定当前删除的菜品中是否关联有套餐，若是，则无法删除；
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids) {
        /*String[] split = ids.split(",");
        List<Long> list = new ArrayList<>();
        for (String str : split) {
            list.add(Long.valueOf(str));
        }*/
        dishService.deleteDishWithFlavor(ids);
        return R.success("删除成功！");
    }

    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable int status, @RequestParam List<Long> ids) {
        Dish dish = new Dish();
        dish.setStatus(status);
        for (Long id : ids) {
            dish.setId(id);
            dishService.updateById(dish);
        }
        return R.success("状态修改成功！");
    }

    /*
     * 添加套餐时，根据请求参数categoryId，查询每一个菜品分类下所有的菜品；
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish) {
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Dish::getStatus, 1);
        queryWrapper.eq(Dish::getCategoryId, dish.getCategoryId());
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list = dishService.list(queryWrapper);

        // 查询菜品对应的口味信息，用于在客户端访问时展示相应的口味信息
        List<DishDto> dishDtos = list.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            LambdaQueryWrapper<DishFlavor> flavorqueryWrapper = new LambdaQueryWrapper<>();
            flavorqueryWrapper.eq(DishFlavor::getDishId, item.getId());
            List<DishFlavor> flavors = dishFlavorService.list(flavorqueryWrapper);
            dishDto.setFlavors(flavors);
            return dishDto;
        }).collect(Collectors.toList());

        return R.success(dishDtos);
    }


}
