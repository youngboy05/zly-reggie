package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;

import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;


    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {
        //1、将页面表单提交的密码进行md5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        //2、根据登录页面表单提交的用户名查询数据库，得到相应的用户信息
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);
        //3、如果没有查询到用户则返回登录失败
        if (emp == null) return R.error("登录失败");
        //4、如果密码不一致则返回登录失败
        if (!emp.getPassword().equals(password)) return R.error("登录失败");
        //5、查看员工状态，如果当前状态为已禁用，则返回员工已禁用；
        if (emp.getStatus() == 0) return R.error("当前用户已被禁用");
        //6、将登录成功的用户信息写入session，返回给浏览器；
        request.getSession().setAttribute("employee", emp.getId());
        return R.success(emp);
    }

    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        request.getSession().removeAttribute("employee"); //将请求中的session信息清除
        return R.success("退出成功！");
    }

    /*
    新增员工
    注意：employee表中username字段加上了unique索引，所以username值不能重复，如果添加的用户名重复，则会产生异常，所以此处就需要用到全局异常处理器来处理异常！
     */
    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee) {
        // 1、设置一些初始信息（status默认是0，不用设置）
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        //employee.setCreateTime(LocalDateTime.now());
        //employee.setUpdateTime(LocalDateTime.now());
        //Long empId = (Long) request.getSession().getAttribute("employee");
        //employee.setCreateUser(empId);
        //employee.setUpdateUser(empId);
        employeeService.save(employee);
        return R.success("添加成功！");
    }

    @GetMapping("/page")
    // 通过fn+12开发者工具，可以抓取到员工管理页面所展示的记录所请求的参数有：page，pageSize，name，请求方式为：get，请求路径为：employee/page
    public R<Page> page(Integer page, Integer pageSize, String name) {
        //1、构造分页构造器 以及 条件构造器(通过name进行条件分页查询)
        //1.1、分页构造器
        Page pageInfo = new Page(page, pageSize); // 即sql语句中的 limit 分页查询部分
        //1.2、条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        /*
        if (name != null) {
            queryWrapper.like(Employee::getName,name); //因为输入的名字不一定是全名，所以更合适用 like 模糊查询
        }*/
        // 添加过滤条件，也就是sql中的 where条件查询部分
        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name); // 与上面的条件判断是等效的
        // 添加排序条件，也就是sql中的 order by部分
        queryWrapper.orderByDesc(Employee::getUpdateTime);
        // 执行查询
        employeeService.page(pageInfo, queryWrapper); // 查询成功后，查询得到的数据会封装在pageInfo中的records集合中，记录数会赋值给total
        return R.success(pageInfo);
    }

    /*
    修改员工信息；
    在员工管理界面给管理员提供了，禁用账号以及修改员工信息两个功能，其本质上也就是update，所以直接定义一个方法即可
     */
    @PutMapping
    public R<String> update(HttpServletRequest request, @RequestBody Employee employee) {
        //employee.setUpdateUser((Long) request.getSession().getAttribute("employee"));
        //employee.setUpdateTime(LocalDateTime.now());
        log.info(employee.toString());
        employeeService.updateById(employee);
        return R.success("修改成功！");
    }

    /**
     * 通过 id 查询并回显员工信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> add(@PathVariable Long id) {
        Employee employee = employeeService.getById(id);
        return employee != null ? R.success(employee) : R.error("没有查询到相关员工信息!");
    }

}
