package com.itheima.reggie.control;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 套餐管理
 */
@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private DishService dishService;
    @Autowired
    private CategoryService categoryService;


    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){
        log.info("套餐信息：{}",setmealDto);
        setmealService.saveWithDish(setmealDto);
        return R.success("新增套餐成功");
    }

    /**
     * 套餐分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        //分页构造器对象
        Page<Setmeal> pageInfo = new Page<>(page,pageSize);
        Page<SetmealDto> dtoPage = new Page<>();
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件，根据name进行like模糊查询
        queryWrapper.like(name != null,Setmeal::getName,name);
        //添加排序条件，根据更新时间降序排列
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        setmealService.page(pageInfo,queryWrapper);
        //对象拷贝
        BeanUtils.copyProperties(pageInfo,dtoPage,"records");
        List<Setmeal> records = pageInfo.getRecords();
        List<SetmealDto> list = records.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            //对象拷贝
            BeanUtils.copyProperties(item,setmealDto);
            //分类id
            Long categoryId = item.getCategoryId();
            //根据分类id查询分类对象
            Category category = categoryService.getById(categoryId);
            if(category != null){
                //分类名称
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;
        }).collect(Collectors.toList());
        dtoPage.setRecords(list);
        return R.success(dtoPage);
    }

    /**
     * 删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        log.info("ids:{}",ids);
        setmealService.removeWithDish(ids);
        return R.success("套餐数据删除成功");
    }

    /**
     * 修改套餐
     * @param
     * @return
     */
    @GetMapping("{id}")
    public R<SetmealDto> getSetmealDto(@PathVariable Long id){
        log.info("可以回显");

        SetmealDto setmealDto = setmealService.getByIdDto(id);

        return R.success(setmealDto);

    }

    @PutMapping
    public R<String> updateDto(@RequestBody SetmealDto setmealDto){
        log.info("可以做修改操作");

        setmealService.updateDto(setmealDto);
        return R.success("修改成功");
    }

    /**
     * 批量更新套餐状态（停售/起售）
     * @param status 目标状态：0-停售，1-起售
     * @param ids 逗号分隔的ID字符串
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable Integer status, @RequestParam String ids) {
        log.info("更新套餐状态为：{}，id为：{}", status, ids);

        try {
            // 参数验证
            if (status == null || (status != 0 && status != 1)) {
                return R.error("状态参数错误，只能为0或1");
            }

            if (ids == null || ids.trim().isEmpty()) {
                return R.error("套餐ID不能为空");
            }

            // 将逗号分隔的字符串转换为Long类型的List
            List<Long> idList = Arrays.stream(ids.split(","))
                    .map(String::trim)
                    .filter(id -> !id.isEmpty())
                    .map(Long::valueOf)
                    .collect(Collectors.toList());

            if (idList.isEmpty()) {
                return R.error("有效的套餐ID不能为空");
            }

            // 创建要更新的套餐对象
            Setmeal setmeal = new Setmeal();
            setmeal.setStatus(status);
            setmeal.setUpdateTime(LocalDateTime.now());

            // 构建更新条件
            LambdaUpdateWrapper<Setmeal> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.in(Setmeal::getId, idList);

            // 执行批量更新
            boolean success = setmealService.update(setmeal, updateWrapper);

            if (success) {
                String statusText = status == 1 ? "起售" : "停售";
                return R.success("套餐" + statusText + "成功，共更新" + idList.size() + "个套餐");
            } else {
                return R.error("套餐状态更新失败");
            }
        } catch (NumberFormatException e) {
            log.error("套餐ID格式错误：{}", ids, e);
            return R.error("套餐ID格式错误，请检查ID格式");
        } catch (Exception e) {
            log.error("更新套餐状态失败：", e);
            return R.error("套餐状态更新失败：" + e.getMessage());
        }
    }

    /**
     * 根据条件查询套餐数据
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId() !=
                null,Setmeal::getCategoryId,setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus() !=
                null,Setmeal::getStatus,setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> list = setmealService.list(queryWrapper);
        return R.success(list);
    }
}