package cn.wolfcode.car.business.web.controller.system;


import cn.wolfcode.car.base.domain.User;
import cn.wolfcode.car.business.domain.CarPackageAudit;
import cn.wolfcode.car.business.domain.Statement;
import cn.wolfcode.car.business.query.CarPackageAuditQuery;
import cn.wolfcode.car.business.service.ICarPackageAuditService;
import cn.wolfcode.car.business.service.IStatementService;
import cn.wolfcode.car.common.base.page.TablePageInfo;
import cn.wolfcode.car.common.web.AjaxResult;
import cn.wolfcode.car.shiro.ShiroUtils;
import org.apache.poi.util.IOUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

/**
 * 养修服务单向控制器
 */
@Controller
@RequestMapping("business/carPackageAudit")
public class CarPackageAuditController {
    //模板前缀
    private static final String prefix = "business/system/carPackageAudit/";

    @Autowired
    private ICarPackageAuditService carPackageAuditService;

    //页面------------------------------------------------------------
    //列表
    @RequiresPermissions("business:carPackageAudit:view")
    @RequestMapping("/listPage")
    public String listPage(){
        return prefix + "list";
    }

    @RequiresPermissions("business:carPackageAudit:add")
    @RequestMapping("/addPage")
    public String addPage(){
        return prefix + "add";
    }


    @RequiresPermissions("business:carPackageAudit:edit")
    @RequestMapping("/editPage")
    public String editPage(Long id, Model model){
        model.addAttribute("carPackageAudit", carPackageAuditService.get(id));
        return prefix + "edit";
    }

    //数据-----------------------------------------------------------
    //列表
    @RequiresPermissions("business:carPackageAudit:list")
    @RequestMapping("/query")
    @ResponseBody
    public TablePageInfo<CarPackageAudit> query(CarPackageAuditQuery qo){
        return carPackageAuditService.query(qo);
    }


    //新增
    @RequiresPermissions("business:carPackageAudit:add")
    @RequestMapping("/add")
    @ResponseBody
    public AjaxResult addSave(CarPackageAudit carPackageAudit){
        carPackageAuditService.save(carPackageAudit);
        return AjaxResult.success();
    }

    //编辑
    @RequiresPermissions("business:carPackageAudit:edit")
    @RequestMapping("/edit")
    @ResponseBody
    public AjaxResult edit(CarPackageAudit carPackageAudit){
        carPackageAuditService.update(carPackageAudit);
        return AjaxResult.success();
    }

    //删除
    @RequiresPermissions("business:carPackageAudit:remove")
    @RequestMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids){
        carPackageAuditService.deleteBatch(ids);
        return AjaxResult.success();
    }

    //查看审核进度
    @RequiresPermissions("business:carPackageAudit:processImg")
    @RequestMapping("/processImg")
    public void processImg(Long id, HttpServletResponse servletResponse) throws IOException {
        InputStream inputStream = carPackageAuditService.processImg(id);
        IOUtils.copy(inputStream,servletResponse.getOutputStream());
    }

    //撤销
    @RequiresPermissions("business:carPackageAudit:cancelApply")
    @RequestMapping("/cancelApply")
    @ResponseBody
    public AjaxResult cancelApply(Long id){
        carPackageAuditService.cancelApply(id);
        return AjaxResult.success();
    }

    //========================================
    //待办
    @RequestMapping("/todoPage")
    public String todoPage(){
        return prefix + "todoPage";
    }

    //待办列表
    @RequestMapping("/todoQuery")
    @ResponseBody
    public TablePageInfo<CarPackageAudit> todoQuery(CarPackageAuditQuery qo){
        User user = ShiroUtils.getUser();
        qo.setAuditorId(user.getId());
        qo.setStatus(CarPackageAudit.STATUS_IN_ROGRESS);
        return carPackageAuditService.query(qo);
    }

    //审批
    @RequestMapping("/auditPage")
    public String auditPage(Long id,Model model){
        model.addAttribute("id",id);
        return prefix + "auditPage";
    }

    //审批确定
    @RequestMapping("/audit")
    @ResponseBody
    public AjaxResult audit(Long id,int auditStatus,String info){
        carPackageAuditService.audit(id,auditStatus,info);
        return AjaxResult.success();
    }

    //已办
    @RequiresPermissions("business:carPackageAudit:donePage")
    @RequestMapping("/donePage")
    public String donePage(){
        return prefix + "donePage";
    }

    //查看已办
    @RequiresPermissions("business:carPackageAudit:doneQuery")
    @RequestMapping("/doneQuery")
    @ResponseBody
    public TablePageInfo<CarPackageAudit> doneQuery(CarPackageAuditQuery qo){
        User user = ShiroUtils.getUser();
        qo.setInfo(user.getUserName());
        return carPackageAuditService.query(qo);
    }
}
