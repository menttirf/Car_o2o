package cn.wolfcode.car.business.web.controller.system;


import cn.wolfcode.car.business.domain.BpmnInfo;
import cn.wolfcode.car.business.query.BpmnInfoQuery;
import cn.wolfcode.car.business.service.IBpmnInfoService;
import cn.wolfcode.car.common.base.page.TablePageInfo;
import cn.wolfcode.car.common.web.AjaxResult;
import org.apache.commons.io.IOUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

/**
 * 审核控制器
 */
@Controller
@RequestMapping("business/bpmnInfo")
public class BpmnInfoController {
    //模板前缀
    private static final String prefix = "business/system/bpmnInfo/";

    @Autowired
    private IBpmnInfoService bpmnInfoService;

    //页面------------------------------------------------------------
    //列表
    @RequiresPermissions("business:bpmnInfo:view")
    @RequestMapping("/listPage")
    public String listPage(){
        return prefix + "list";
    }

    //数据-----------------------------------------------------------
    //列表
    @RequiresPermissions("business:bpmnInfo:list")
    @RequestMapping("/query")
    @ResponseBody
    public TablePageInfo<BpmnInfo> query(BpmnInfoQuery qo){
        return bpmnInfoService.query(qo);
    }

    //删除
    @RequiresPermissions("business:bpmnInfo:delete")
    @RequestMapping("/delete")
    @ResponseBody
    public AjaxResult delete(Long id){
        bpmnInfoService.deleteBatch(id);
        return AjaxResult.success();
    }

    //流程文件部署
    @RequiresPermissions("business:bpmnInfo:deployPage")
    @RequestMapping("/deployPage")
    public String deployPage(){
        return prefix + "deploy";
    }

    //流程文件上传
    @RequiresPermissions("business:bpmnInfo:upload")
    @RequestMapping("/upload")
    @ResponseBody
    public AjaxResult upload(MultipartFile file){
        String path = bpmnInfoService.saveUploadFile(file);
        return AjaxResult.success("操作成功",path);
    }

    //部署保存
    @RequiresPermissions("business:bpmnInfo:deploy")
    @RequestMapping("/deploy")
    @ResponseBody
    public AjaxResult deploy(BpmnInfo bpmnInfo) throws IOException {
        bpmnInfoService.saveDeploy(bpmnInfo);
        return AjaxResult.success();
    }

    //编辑
    @RequiresPermissions("business:bpmnInfo:editPage")
    @RequestMapping("/editPage")
    public String editPage(Long id,Model model) {
        BpmnInfo bpmnInfo = bpmnInfoService.get(id);
        model.addAttribute("bpmnInfo",bpmnInfo);
        return prefix + "edit";
    }

    //编辑确定
    @RequiresPermissions("business:bpmnInfo:edit")
    @RequestMapping("/edit")
    @ResponseBody
    public AjaxResult edit(BpmnInfo bpmnInfo) {
        bpmnInfoService.updateEditPage(bpmnInfo);
        return AjaxResult.success();
    }

    //查看流程文件
    @RequiresPermissions("business:bpmnInfo:readResource")
    @RequestMapping("/readResource")
    @ResponseBody
    public void readResource(String deploymentId, String type, HttpServletResponse servletResponse) throws IOException {
        InputStream inputStream = bpmnInfoService.getReadResource(deploymentId,type);
        IOUtils.copy(inputStream,servletResponse.getOutputStream());
    }
}