package cn.wolfcode.car.business.web.controller.system;


import cn.wolfcode.car.base.service.IUserService;
import cn.wolfcode.car.business.domain.Statement;
import cn.wolfcode.car.business.domain.StatementItem;
import cn.wolfcode.car.business.query.StatementQuery;
import cn.wolfcode.car.business.service.IStatementItemService;
import cn.wolfcode.car.business.service.IStatementService;
import cn.wolfcode.car.common.base.page.TablePageInfo;
import cn.wolfcode.car.common.web.AjaxResult;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 服务结算单控制器
 */
@Controller
@RequestMapping("business/statementItem")
public class StatementItemController {
    //模板前缀
    private static final String prefix = "business/system/statement/";

    @Autowired
    private IStatementService statementService;

    @Autowired
    private IStatementItemService statementItemService;

    @Autowired
    private IUserService userService;

    //明细
    @RequiresPermissions("business:statement:itemDetail")
    @RequestMapping("/itemDetail")
    public String itemDetail(Long statementId,Model model) {
        //判断服务状态判断跳转路径
        Statement statement = statementService.selectByStatementId(statementId);
        model.addAttribute("statement",statement);
        if (Statement.STATUS_CONSUME.equals(statement.getStatus())){
            //服务状态：消费中
            return prefix + "editDetail";
        }else {
            statement.setPayee(userService.get(statement.getPayeeId()));
            //服务状态：已支付
            return prefix + "showDetail";
        }
    }

    // query
    @RequiresPermissions("business:statement:query")
    @RequestMapping("/query")
    @ResponseBody
    public TablePageInfo<StatementItem> query(StatementQuery qo, Long statementId) {
        return statementItemService.query(qo, statementId);
    }

    //保存 saveItems
    @RequiresPermissions("business:statement:saveItems")
    @RequestMapping("/saveItems")
    @ResponseBody
    public AjaxResult saveItems(@RequestBody List<StatementItem> statementItems){
        statementItemService.saveItems(statementItems);
        return AjaxResult.success();
    }

    //确定支付 payStatement
    @RequiresPermissions("business:statement:payStatement")
    @RequestMapping("/payStatement")
    @ResponseBody
    public AjaxResult payStatement(Long statementId){
        statementItemService.payStatement(statementId);
        return AjaxResult.success();
    }
}
