package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    public void saveSetmealWithSetmealDish(SetmealDto setmealDto);

    public void deleteSetmealWithSetmealDish(List<Long> ids);

    public SetmealDto backShow(Long id);

    public void updateSetmealWithSetmealDish(SetmealDto setmealDto);
}
