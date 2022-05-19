package cn.wolfcode.car.business.web.controller.system;


import cn.wolfcode.car.base.service.IDeptService;
import cn.wolfcode.car.business.domain.Department;
import cn.wolfcode.car.business.domain.Employee;
import cn.wolfcode.car.business.domain.Statement;
import cn.wolfcode.car.business.query.EmployeeQuery;
import cn.wolfcode.car.business.service.IDepartmentService;
import cn.wolfcode.car.business.service.IEmployeeService;
import cn.wolfcode.car.business.service.IStatementService;
import cn.wolfcode.car.common.base.page.TablePageInfo;
import cn.wolfcode.car.common.web.AjaxResult;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 养修服务单向控制器
 */
@Controller
@RequestMapping("business/employee")
public class EmployeeController {
    //模板前缀
    private static final String prefix = "business/system/employee/";

    @Autowired
    private IEmployeeService employeeService;

    @Autowired
    private IDepartmentService departmentService;

    //页面------------------------------------------------------------
    //列表
    @RequiresPermissions("business:employee:view")
    @RequestMapping("/listPage")
    public String listPage(Model model){
        List<Department> departments = departmentService.list();
        model.addAttribute("departments",departments);
        return prefix + "list";
    }

    @RequiresPermissions("business:employee:add")
    @RequestMapping("/addPage")
    public String addPage(Model model){
        List<Department> departments = departmentService.list();
        model.addAttribute("departments",departments);
        return prefix + "add";
    }


    @RequiresPermissions("business:employee:edit")
    @RequestMapping("/editPage")
    public String editPage(Long id, Model model){
        model.addAttribute("employee", employeeService.get(id));
        List<Department> departments = departmentService.list();
        model.addAttribute("departments",departments);
        return prefix + "edit";
    }

    //数据-----------------------------------------------------------
    //列表
    @RequiresPermissions("business:employee:list")
    @RequestMapping("/query")
    @ResponseBody
    public TablePageInfo<Employee> query(EmployeeQuery qo){
        return employeeService.query(qo);
    }


    //新增
    @RequiresPermissions("business:employee:add")
    @RequestMapping("/add")
    @ResponseBody
    public AjaxResult addSave(Employee employee){
        employeeService.save(employee);
        return AjaxResult.success();
    }

    //编辑
    @RequiresPermissions("business:employee:edit")
    @RequestMapping("/edit")
    @ResponseBody
    public AjaxResult edit(Employee employee){
        employeeService.update(employee);
        return AjaxResult.success();
    }

    //删除
    @RequiresPermissions("business:employee:remove")
    @RequestMapping("/remove")
    @ResponseBody
    public AjaxResult remove(Long id){
        employeeService.delete(id);
        return AjaxResult.success();
    }


    @RequiresPermissions("business:employee:selectDetail")
    @RequestMapping("/selectDetail")
    public String selectDetail(Long id,Model model){
        Employee employee = employeeService.get(id);
        Department department = departmentService.get(employee.getDepartment().getId());
        model.addAttribute("department",department);
        return prefix + "selectDetail";
    }
}
