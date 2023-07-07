package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/setmeal")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private CategoryService categoryService;

    // 添加套餐
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        setmealService.saveSetmealWithSetmealDish(setmealDto);
        return R.success("添加成功！");
    }

    // 分页显示
    @GetMapping("/page")
    public R<Page> page(String name, Integer page, Integer pageSize) {
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name != null, Setmeal::getName, name);
        setmealService.page(pageInfo, queryWrapper);

        Page<SetmealDto> setmealDtoPage = new Page<>();
        BeanUtils.copyProperties(pageInfo, setmealDtoPage);

        List<Setmeal> records = pageInfo.getRecords();

        List<SetmealDto> list1 = records.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto);
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            setmealDto.setCategoryName(category.getName());
            return setmealDto;
        }).collect(Collectors.toList());

        setmealDtoPage.setRecords(list1);
        return R.success(setmealDtoPage);
    }

    /*
     * 删除套餐：需要确定当前套餐是否正在出售，出售状态则无法删除；
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        setmealService.deleteSetmealWithSetmealDish(ids);
        return R.success("删除成功！");
    }

    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable Integer status, @RequestParam List<Long> ids){
        Setmeal setmeal = new Setmeal();
        setmeal.setStatus(status);
        for (Long id : ids) {
            setmeal.setId(id);
            setmealService.updateById(setmeal);
        }
        return R.success("状态修改成功！");
    }

    /*
     * 展示套餐内容
     */
    @GetMapping("/list")
    public R<List<SetmealDto>> list(Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Setmeal::getCategoryId,setmeal.getCategoryId());
        queryWrapper.eq(Setmeal::getStatus,setmeal.getStatus());
        queryWrapper.orderByAsc(Setmeal::getPrice).orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> setmealList = setmealService.list(queryWrapper);

        // 查询套餐中包含的菜品信息，用于在客户端访问时展示相应的菜品信息
        List<SetmealDto> setmealDtoList = setmealList.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto);
            LambdaQueryWrapper<SetmealDish> flavorqueryWrapper = new LambdaQueryWrapper<>();
            flavorqueryWrapper.eq(SetmealDish::getSetmealId, item.getId());
            List<SetmealDish> list = setmealDishService.list(flavorqueryWrapper);
            setmealDto.setSetmealDishes(list);
            return setmealDto;
        }).collect(Collectors.toList());
        return R.success(setmealDtoList);
    }

    /**
     * 页面回显
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<SetmealDto> showBack(@PathVariable Long id){
        SetmealDto setmealDto = setmealService.backShow(id);
        return R.success(setmealDto);
    }


    /**
     * 修改套餐信息
     * @param setmealDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto){
        setmealService.updateSetmealWithSetmealDish(setmealDto);
        return R.success("保存成功！");
    }
}
