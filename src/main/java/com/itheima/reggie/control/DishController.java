package com.itheima.reggie.control;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜品管理
 */
@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
     @Autowired
     private DishService dishService;
     @Autowired
     private DishFlavorService dishFlavorService;

     @Autowired
     private CategoryService categoryService;

     /**
      * 新增菜品
      * @param dishDto
      * @return
      */
     @PostMapping
     public R<String> save(@RequestBody DishDto dishDto){
          log.info(dishDto.toString());
          dishService.saveWithFlavor(dishDto);
          return R.success("新增菜品成功");
     }

     /**
      * 菜品信息分页查询
      * @param page
      * @param pageSize
      * @param name
      * @return
      */
     @GetMapping("/page")
     public R<Page> page(int page, int pageSize, String name){
          //构造分页构造器对象
          Page<Dish> pageInfo = new Page<>(page,pageSize);
          Page<DishDto> dishDtoPage = new Page<>();
          //条件构造器
          LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
          //添加过滤条件
          queryWrapper.like(name != null,Dish::getName,name);
          //添加排序条件
          queryWrapper.orderByDesc(Dish::getUpdateTime);
          //执行分页查询
          dishService.page(pageInfo,queryWrapper);//对象拷贝
          BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");
          List<Dish> records = pageInfo.getRecords();
          List<DishDto> list = records.stream().map((item) -> {

               DishDto dishDto = new DishDto();
               BeanUtils.copyProperties(item,dishDto);
               Long categoryId = item.getCategoryId();//分类id
               //根据id查询分类对象
               Category category = categoryService.getById(categoryId);

               if(category != null){
                    String categoryName = category.getName();
                    dishDto.setCategoryName(categoryName);
               }
               return dishDto;
          }).collect(Collectors.toList());
          dishDtoPage.setRecords(list);

          return R.success(dishDtoPage);
     }

//     /**
//      * 根据id删除菜品
//      * @param ids 逗号分隔的ID字符串
//      * @return
//      */
//     @DeleteMapping
//     public R<String> delete(@RequestParam String ids){
//          log.info("删除菜品，id为：{}", ids);
//
//          try {
//               // 将逗号分隔的字符串转换为Long类型的List
//               List<Long> idList = Arrays.stream(ids.split(","))
//                       .map(String::trim)
//                       .map(Long::valueOf)
//                       .collect(Collectors.toList());
//
//               // 检查所有菜品状态，如果有在售(status=1)的菜品则不允许删除
//               LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
//               queryWrapper.in(Dish::getId, idList);
//               queryWrapper.eq(Dish::getStatus, 1); // 查询在售状态的菜品
//
//               int onSaleCount = dishService.count(queryWrapper);
//               if (onSaleCount > 0) {
//                    return R.error("存在正在售卖的菜品，请先停售后再删除");
//               }
//
//               // 调用批量删除
//               boolean success = dishService.removeByIds(idList);
//
//               if (success) {
//                    return R.success("菜品删除成功");
//               } else {
//                    return R.error("菜品删除失败");
//               }
//          } catch (NumberFormatException e) {
//               log.error("ID格式错误：{}", ids);
//               return R.error("ID格式错误");
//          } catch (Exception e) {
//               log.error("删除菜品失败：", e);
//               return R.error("删除失败：" + e.getMessage());
//          }
//     }


     /**
      * 删除菜品
      * @param ids
      * @return
      */
     @DeleteMapping
     public R<String> deleteDish(@RequestParam List<Long> ids){
          log.info(ids.toString());
          //删除操作
          dishService.deleteDish(ids);
          return R.success("删除成功");
     }

     /**
      * 批量更新菜品状态（停售/起售）
      * @param status 目标状态：0-停售，1-起售
      * @param ids 逗号分隔的ID字符串
      * @return
      */
     @PostMapping("/status/{status}")
     public R<String> updateStatus(@PathVariable Integer status, @RequestParam List<Long> ids) {
          log.info("更新菜品状态为：{}，id为：{}", status, ids);

               boolean success = dishService.updateStatus(status,ids);

               if (success) {
                    String statusText = status == 1 ? "起售" : "停售";
                    return R.success("菜品" + statusText + "成功");
               } else {
                    return R.error("状态更新失败");
               }
     }


//     /**
//      * 批量更新菜品状态（停售/起售）
//      * @param status 目标状态：0-停售，1-起售
//      * @param ids 逗号分隔的ID字符串
//      * @return
//      */
//     @PostMapping("/status/{status}")
//     public R<String> updateStatus(@PathVariable Integer status, @RequestParam String ids) {
//          log.info("更新菜品状态为：{}，id为：{}", status, ids);
//
//          try {
//               // 将逗号分隔的字符串转换为Long类型的List
//               List<Long> idList = Arrays.stream(ids.split(","))
//                       .map(String::trim)
//                       .map(Long::valueOf)
//                       .collect(Collectors.toList());
//
//               // 创建要更新的菜品对象
//               Dish dish = new Dish();
//               dish.setStatus(status);
//               dish.setUpdateTime(LocalDateTime.now());
//
//               // 构建更新条件
//               LambdaQueryWrapper<Dish> updateWrapper = new LambdaQueryWrapper<>();
//               updateWrapper.in(Dish::getId, idList);
//
//               // 执行批量更新
//               boolean success = dishService.update(dish, updateWrapper);
//
//               if (success) {
//                    String statusText = status == 1 ? "起售" : "停售";
//                    return R.success("菜品" + statusText + "成功");
//               } else {
//                    return R.error("状态更新失败");
//               }
//          } catch (NumberFormatException e) {
//               log.error("ID格式错误：{}", ids);
//               return R.error("ID格式错误");
//          } catch (Exception e) {
//               log.error("更新菜品状态失败：", e);
//               return R.error("状态更新失败：" + e.getMessage());
//          }
//     }

     /**
      * 根据id查询菜品信息和对应的口味信息
      * @param id
      * @return
      */
     @GetMapping("/{id}")
     public R<DishDto> get(@PathVariable Long id){
          DishDto dishDto = dishService.getByIdWithFlavor(id);
          return R.success(dishDto);
     }

     /**
      * 修改菜品
      * @param dishDto
      * @return
      */
     @PutMapping
     public R<String> update(@RequestBody DishDto dishDto){
          log.info(dishDto.toString());
          dishService.updateWithFlavor(dishDto);
          return R.success("修改菜品成功");
     }

     /**
      * 根据条件查询对应的菜品数据
      * @param dish
      * @return
      */
     @GetMapping("/list")
     public R<List<DishDto>> list(Dish dish){
          //构造查询条件
          LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
          queryWrapper.eq(dish.getCategoryId() != null,Dish::getCategoryId,dish.getCategoryId());
          //添加条件，查询状态为1（起售状态）的菜品
          queryWrapper.eq(Dish::getStatus,1);
          //添加排序条件
          queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

          List<Dish> list = dishService.list(queryWrapper);

          List<DishDto> dishDtoList = list.stream().map((item) -> {
               DishDto dishDto = new DishDto();
               BeanUtils.copyProperties(item,dishDto);
               Long categoryId = item.getCategoryId();//分类id
               //根据id查询分类对象
               Category category = categoryService.getById(categoryId);
               if(category != null){
                    String categoryName = category.getName();
                    dishDto.setCategoryName(categoryName);
               }
               //当前菜品的id
               Long dishId = item.getId();
               LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
               lambdaQueryWrapper.eq(DishFlavor::getDishId,dishId);
               //SQL:select * from dish_flavor where dish_id = ?
               List<DishFlavor> dishFlavorList = dishFlavorService.list(lambdaQueryWrapper);
               dishDto.setFlavors(dishFlavorList);

               return dishDto;
          }).collect(Collectors.toList());
          return R.success(dishDtoList);
     }

} 