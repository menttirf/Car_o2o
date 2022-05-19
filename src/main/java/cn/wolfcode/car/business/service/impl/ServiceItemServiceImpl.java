package cn.wolfcode.car.business.service.impl;

import cn.wolfcode.car.business.domain.BpmnInfo;
import cn.wolfcode.car.business.domain.CarPackageAudit;
import cn.wolfcode.car.business.domain.ServiceItem;
import cn.wolfcode.car.business.mapper.ServiceItemMapper;
import cn.wolfcode.car.business.query.ServiceItemQuery;
import cn.wolfcode.car.business.service.IBpmnInfoService;
import cn.wolfcode.car.business.service.ICarPackageAuditService;
import cn.wolfcode.car.business.service.IServiceItemService;
import cn.wolfcode.car.common.base.page.TablePageInfo;
import cn.wolfcode.car.common.exception.BusinessException;
import cn.wolfcode.car.common.util.Convert;
import cn.wolfcode.car.shiro.ShiroUtils;
import com.github.pagehelper.PageHelper;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class ServiceItemServiceImpl implements IServiceItemService {

    @Autowired
    private ServiceItemMapper serviceItemMapper;

    @Autowired
    private ICarPackageAuditService carPackageAuditService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private IBpmnInfoService bpmnInfoService;

    @Override
    public TablePageInfo<ServiceItem> query(ServiceItemQuery qo) {
        PageHelper.startPage(qo.getPageNum(), qo.getPageSize());
        return new TablePageInfo<ServiceItem>(serviceItemMapper.selectForList(qo));
    }


    @Override
    public void save(ServiceItem serviceItem) {
        serviceItem.setCreateTime(new Date());
        /*
        * 业务要求:如果是套餐服务需要店长、财务审批之后才可以上架销售
        * 判断是否是套餐服务
        * */
        //->如果是套餐,则审核状态为AUDITSTATUS_INIT
        if (serviceItem.getCarPackage()){
            serviceItem.setAuditStatus(ServiceItem.AUDITSTATUS_INIT);
        }else {
            //->如果不是套餐，则审核状态为AUDITSTATUS_NO_REQUIRED
            serviceItem.setAuditStatus(ServiceItem.AUDITSTATUS_NO_REQUIRED);
        }
        //->默认上架状态为 下架
        serviceItem.setSaleStatus(ServiceItem.SALESTATUS_OFF);
        serviceItemMapper.insert(serviceItem);
    }

    @Override
    public ServiceItem get(Long id) {
        return serviceItemMapper.selectByPrimaryKey(id);
    }


    @Override
    public void update(ServiceItem serviceItem) {
        //需求：当页面审核状态数据为审核中，不能修改
        ServiceItem serviceItemData = serviceItemMapper.selectByPrimaryKey(serviceItem.getId());
        if (serviceItemData == null) {
            throw new BusinessException("参数异常");
        }
        if (!ServiceItem.AUDITSTATUS_NO_REQUIRED.equals(serviceItemData.getAuditStatus())){
            throw new BusinessException("确认审核中不能修改");
        }
        if (!ServiceItem.SALESTATUS_OFF.equals(serviceItemData.getSaleStatus())){
            throw new BusinessException("上架中不能修改");
        }
        serviceItemMapper.updateByPrimaryKey(serviceItem);
    }

    @Override
    public void deleteBatch(String ids) {
        Long[] dictIds = Convert.toLongArray(ids);
        for (Long dictId : dictIds) {
            serviceItemMapper.deleteByPrimaryKey(dictId);
        }
    }

    @Override
    public List<ServiceItem> list() {
        return serviceItemMapper.selectAll();
    }

    //上架
    @Override
    public void updateBySaleOn(Long id) {
        ServiceItem serviceItemData = serviceItemMapper.selectByPrimaryKey(id);
        if (serviceItemData == null || ServiceItem.SALESTATUS_ON.equals(serviceItemData.getSaleStatus())){
            throw new BusinessException("参数异常");
        }
        //是否时套餐 --> 否,上架状态为上架
        if (!serviceItemData.getCarPackage()){
            serviceItemMapper.updateByCarPackage(id,ServiceItem.SALESTATUS_ON);
            return;
        }
        //如果是套餐---需要审核通过状态才可以上架
        if (ServiceItem.AUDITSTATUS_REPLY.equals(serviceItemData.getAuditStatus())) {
            serviceItemMapper.updateByCarPackage(id,ServiceItem.SALESTATUS_ON);
            return;
        }else{
            throw new BusinessException("审核通过才能上架");
        }

    }

    //下架
    @Override
    public void updateBySalesaleOff(Long id) {
        ServiceItem serviceItemData = serviceItemMapper.selectByPrimaryKey(id);
        if (serviceItemData == null || ServiceItem.SALESTATUS_OFF.equals(serviceItemData.getSaleStatus())){
            throw new BusinessException("参数异常");
        }
        serviceItemMapper.updateByCarPackage(id,ServiceItem.SALESTATUS_OFF);
    }

    @Override
    public TablePageInfo<ServiceItem> selectAllSaleOnList(ServiceItemQuery qo) {
        PageHelper.startPage(qo.getPageNum(), qo.getPageSize());
        return new TablePageInfo<>(serviceItemMapper.selectAllSaleOnList(qo));
    }

    //确定发起审核
    @Override
    public void startAudit(Long id, Long bpmnInfoId, Long director,Long finance, String info) {
        //1> id肯能为 空 的情况，审核状态不为 初始化 情况
        ServiceItem serviceItem = serviceItemMapper.selectByPrimaryKey(id);
        if (id == null || !ServiceItem.AUDITSTATUS_INIT.equals(serviceItem.getAuditStatus())){
            throw new BusinessException("参数异常");
        }

        //2> 确定发起审核,修该审核状态为 审核中
        serviceItemMapper.updateByAuditStatus(id,ServiceItem.AUDITSTATUS_AUDITING);

        //3> 将数据添加到套餐审核列表
        CarPackageAudit carPackageAudit = new CarPackageAudit();
        carPackageAudit.setServiceItemId(id); //服务单项id
        carPackageAudit.setServiceItemInfo(serviceItem.getInfo()); ////服务单项备注
        carPackageAudit.setInfo(info);//服务单项备注
        carPackageAudit.setServiceItemPrice(serviceItem.getDiscountPrice());   //服务单项审核价格,优惠后的价格
        carPackageAudit.setCreator(ShiroUtils.getUser().getUserName()); //创建者
        carPackageAudit.setCreateTime(new Date());   //创建时间
        carPackageAudit.setBpmnInfoId(bpmnInfoId);  //关联流程id
        carPackageAudit.setAuditorId(director);   //当前审核人id
        carPackageAuditService.saveStartAudit(carPackageAudit);

        //>4 流程图内的参数变量，店长id ，财务id，金额
        Map<String, Object> map = new HashMap<>();
        if (director != null){
            map.put("director",director.toString());
        }
        if (finance != null){
            map.put("finance",finance.toString());
        }
        map.put("discountPrice",serviceItem.getDiscountPrice().longValue());

        BpmnInfo bpmnInfo = bpmnInfoService.get(bpmnInfoId);

        //5>启动流程
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(bpmnInfo.getActProcessKey(), carPackageAudit.getId().toString(), map);

        //6>更新记录实例
        carPackageAudit.setInstanceId(processInstance.getId());
        carPackageAuditService.updateStartAudit(carPackageAudit);
    }

    @Override
    public void updateByAuditStatus(Long serviceItemId, Integer auditStatusInit) {
        serviceItemMapper.updateByAuditStatus(serviceItemId,auditStatusInit);
    }
}
