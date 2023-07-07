package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private SetmealDishService setmealDishService;

    @Override
    @Transactional // 涉及多张表，需要加入事务控制
    public void saveWithFlavor(DishDto dishDto) {
        // 将dishDto中涉及到的dish表的字段插入dish表中，且成功插入后，自生成的 id等值会赋值给dishDto的相应字段；
        this.save(dishDto);
        // 获取菜品id
        Long dishId = dishDto.getId();
        //将与dish_flavor表有关的字段信息封装到表中
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item)->{ // 为每个DishFlavor对象，将菜品id的值赋值
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());
        // 批量保存
        dishFlavorService.saveBatch(flavors);
    }

    @Override
    @Transactional
    public DishDto getByIdWithFlavor(Long id) {
        Dish dish = this.getById(id);
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish,dishDto);

        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,id);
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(flavors);

        return dishDto;
    }

    @Override
    @Transactional
    public void updateDishWithFlavor(DishDto dishDto) {
        this.updateById(dishDto);  //更新dish表信息

        // 由于前端可以对口味进行删减，所以对于口味表的更新，需要先清理之前的口味信息，再将新的信息保存
        List<DishFlavor> flavors = dishDto.getFlavors();
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(queryWrapper); //清理之前的信息

        // 前端传来的封装在flavors集合的数据中，不包含 DishId信息，所以需要将其添加进去；
        flavors.stream().map((item)->{
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(flavors); // 更新dish_flavor表的信息
    }

    /*
     * 删除菜品信息，需要注意的是，一旦该菜品关联了某个套餐就不能被删除，此时如果是多选删除，都不能删除;
     */
    @Override
    @Transactional
    public void deleteDishWithFlavor(List<Long> ids) {
        LambdaQueryWrapper<SetmealDish> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.in(SetmealDish::getDishId,ids);
        int count1 = setmealDishService.count(queryWrapper1);
        if (count1 >0){
            throw new CustomException("删除的菜品中包含已关联套餐的菜品，无法进行删除");
        }

        LambdaQueryWrapper<Dish> queryWrapper2 = new LambdaQueryWrapper<>();
        queryWrapper2.in(Dish::getId,ids);
        queryWrapper2.eq(Dish::getStatus,1);
        int count2 = this.count(queryWrapper2);
        if (count2 > 0){
            throw new CustomException("删除的菜品中包含正在出售的菜品，无法进行删除");
        }

        this.removeByIds(ids);

        LambdaQueryWrapper<DishFlavor> queryWrapper3 = new LambdaQueryWrapper<>();
        queryWrapper3.in(DishFlavor::getDishId,ids);
        dishFlavorService.remove(queryWrapper3);
    }
}
