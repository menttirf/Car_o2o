package cn.wolfcode.car.business.web.controller.system;


import cn.wolfcode.car.base.service.IUserService;
import cn.wolfcode.car.business.domain.BpmnInfo;
import cn.wolfcode.car.business.domain.ServiceItem;
import cn.wolfcode.car.business.query.ServiceItemQuery;
import cn.wolfcode.car.business.service.IBpmnInfoService;
import cn.wolfcode.car.business.service.IServiceItemService;
import cn.wolfcode.car.common.base.page.TablePageInfo;
import cn.wolfcode.car.common.web.AjaxResult;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 养修服务单向控制器
 */
@Controller
@RequestMapping("business/serviceItem")
public class ServiceItemController {
    //模板前缀
    private static final String prefix = "business/system/serviceItem/";

    @Autowired
    private IServiceItemService serviceItemService;

    @Autowired
    private IUserService userService;

    @Autowired
    private IBpmnInfoService bpmnInfoService;

    //页面------------------------------------------------------------
    //列表
    @RequiresPermissions("business:serviceItem:view")
    @RequestMapping("/listPage")
    public String listPage(){
        return prefix + "list";
    }

    @RequiresPermissions("business:serviceItem:add")
    @RequestMapping("/addPage")
    public String addPage(){
        return prefix + "add";
    }


    @RequiresPermissions("business:serviceItem:edit")
    @RequestMapping("/editPage")
    public String editPage(Long id, Model model){
        model.addAttribute("serviceItem", serviceItemService.get(id));
        return prefix + "edit";
    }

    //数据-----------------------------------------------------------
    //列表
    @RequiresPermissions("business:serviceItem:list")
    @RequestMapping("/query")
    @ResponseBody
    public TablePageInfo<ServiceItem> query(ServiceItemQuery qo){
        return serviceItemService.query(qo);
    }


    //新增
    @RequiresPermissions("business:serviceItem:add")
    @RequestMapping("/add")
    @ResponseBody
    public AjaxResult addSave(ServiceItem serviceItem){
        serviceItemService.save(serviceItem);
        return AjaxResult.success();
    }

    //编辑
    @RequiresPermissions("business:serviceItem:edit")
    @RequestMapping("/edit")
    @ResponseBody
    public AjaxResult edit(ServiceItem serviceItem){
        serviceItemService.update(serviceItem);
        return AjaxResult.success();
    }

    //删除
    @RequiresPermissions("business:serviceItem:remove")
    @RequestMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids){
        serviceItemService.deleteBatch(ids);
        return AjaxResult.success();
    }

    //上架
    @RequiresPermissions("business:serviceItem:saleOn")
    @RequestMapping("/saleOn")
    @ResponseBody
    public AjaxResult saleOn(Long id){
        serviceItemService.updateBySaleOn(id);
        return AjaxResult.success();
    }

    //下架
    @RequiresPermissions("business:serviceItem:saleOff")
    @RequestMapping("/saleOff")
    @ResponseBody
    public AjaxResult saleOff(Long id){
        serviceItemService.updateBySalesaleOff(id);
        return AjaxResult.success();
    }

    //->
    //服务项列表
    @RequiresPermissions("business:serviceItem:selectAllSaleOnList")
    @RequestMapping("/selectAllSaleOnList")
    @ResponseBody
    public TablePageInfo<ServiceItem> selectAllSaleOnList(ServiceItemQuery qo){
        return serviceItemService.selectAllSaleOnList(qo);
    }

    //发起审核
    @RequiresPermissions("business:serviceItem:auditPage")
    @RequestMapping("/auditPage")
    public String auditPage(Long id,Model model){
        model.addAttribute("serviceItem",serviceItemService.get(id));
        //当价格大于3000时需店长审核后再由财务审核
        //店长
        model.addAttribute("directors",userService.queryByRoleKey("shopOwner"));
        //财务
        model.addAttribute("finances",userService.queryByRoleKey("financial"));
        //审核流程
        BpmnInfo bpmnInfo = bpmnInfoService.selectByBpmnType("car_package");
        model.addAttribute("bpmnInfo",bpmnInfo);
        return prefix + "audit";
    }

    //确定发起审核
    @RequiresPermissions("business:serviceItem:startAudit")
    @RequestMapping("/startAudit")
    @ResponseBody
    public AjaxResult startAudit(Long id,Long bpmnInfoId,Long director,Long finance,String info){
        serviceItemService.startAudit(id,bpmnInfoId,director,finance,info);
        return AjaxResult.success();
    }

}
