package cn.wolfcode.car.business.service.impl;

import cn.wolfcode.car.business.domain.BpmnInfo;
import cn.wolfcode.car.business.domain.CarPackageAudit;
import cn.wolfcode.car.business.domain.ServiceItem;
import cn.wolfcode.car.business.mapper.CarPackageAuditMapper;
import cn.wolfcode.car.business.query.CarPackageAuditQuery;
import cn.wolfcode.car.business.service.IBpmnInfoService;
import cn.wolfcode.car.business.service.ICarPackageAuditService;
import cn.wolfcode.car.business.service.IServiceItemService;
import cn.wolfcode.car.business.service.IStatementService;
import cn.wolfcode.car.common.base.page.TablePageInfo;
import cn.wolfcode.car.common.exception.BusinessException;
import cn.wolfcode.car.common.util.Convert;
import cn.wolfcode.car.shiro.ShiroUtils;
import com.github.pagehelper.PageHelper;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.activiti.image.ProcessDiagramGenerator;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.crypto.Data;
import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class CarPackageAuditServiceImpl implements ICarPackageAuditService {

    @Autowired
    private CarPackageAuditMapper carPackageAuditMapper;

    @Autowired
    private IBpmnInfoService bpmnInfoService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private IServiceItemService serviceItemService;

    @Override
    public TablePageInfo<CarPackageAudit> query(CarPackageAuditQuery qo) {
        PageHelper.startPage(qo.getPageNum(), qo.getPageSize());
        return new TablePageInfo<CarPackageAudit>(carPackageAuditMapper.selectForList(qo));
    }


    //??????
    @Override
    public void save(CarPackageAudit carPackageAudit) {
        carPackageAuditMapper.insert(carPackageAudit);
    }

    @Override
    public CarPackageAudit get(Long id) {
        return carPackageAuditMapper.selectByPrimaryKey(id);
    }


    @Override
    public void update(CarPackageAudit carPackageAudit) {
        carPackageAuditMapper.updateByPrimaryKey(carPackageAudit);
    }

    @Override
    public void deleteBatch(String ids) {
        Long[] dictIds = Convert.toLongArray(ids);
        for (Long dictId : dictIds) {
            carPackageAuditMapper.deleteByPrimaryKey(dictId);
        }
    }

    @Override
    public List<CarPackageAudit> list() {
        return carPackageAuditMapper.selectAll();
    }

    @Override
    public void saveStartAudit(CarPackageAudit carPackageAudit) {
        carPackageAuditMapper.saveStartAudit(carPackageAudit);
    }

    @Override
    public void updateStartAudit(CarPackageAudit carPackageAudit) {
        carPackageAuditMapper.updateStartAudit(carPackageAudit);
    }

    @Override
    public InputStream processImg(Long id) {
        CarPackageAudit audit = carPackageAuditMapper.selectByPrimaryKey(id);
        BpmnInfo bpmnInfo = bpmnInfoService.get(audit.getBpmnInfoId());

        InputStream inputStream = null;
        //?????????????????????????????????????????????
        BpmnModel model = repositoryService.getBpmnModel(bpmnInfo.getActProcessId());
        ProcessDiagramGenerator generator = new DefaultProcessDiagramGenerator();
        //1:?????????
        if(CarPackageAudit.STATUS_IN_ROGRESS.equals(audit.getStatus())){

            Task task = taskService.createTaskQuery()
                    .processInstanceId(audit.getInstanceId()).singleResult();
            List<String> activeActivityIds =
                    runtimeService.getActiveActivityIds(task.getExecutionId());

            inputStream = generator.generateDiagram(model, activeActivityIds,
                    Collections.EMPTY_LIST,
                    "??????","??????","??????");
        }else{
            //2???????????????
            inputStream = generator.generateDiagram(model, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                    "??????","??????","??????");
        }
        return inputStream;
    }

    //??????
    @Override
    public void cancelApply(Long id) {
        CarPackageAudit carPackageAudit = carPackageAuditMapper.selectByPrimaryKey(id);
        if (!CarPackageAudit.STATUS_IN_ROGRESS.equals(carPackageAudit.getStatus())){
            throw new BusinessException("???????????????????????????????????????");
        }

        //?????????????????????????????????
        serviceItemService.updateByAuditStatus(carPackageAudit.getServiceItemId(), ServiceItem.AUDITSTATUS_INIT);

        //????????????????????????????????????
        carPackageAuditMapper.updateByAuditStatus(id,CarPackageAudit.STATUS_CANCEL);

        //???????????????
        runtimeService.deleteProcessInstance(carPackageAudit.getInstanceId(),"??????????????????");
    }

    @Override
    public void audit(Long id, int auditStatus, String info) {
        CarPackageAudit carPackageAudit = carPackageAuditMapper.selectByPrimaryKey(id);
        if (carPackageAudit == null || !CarPackageAudit.STATUS_IN_ROGRESS.equals(carPackageAudit.getStatus())){
            throw new BusinessException("????????????");
        }

        String userName = ShiroUtils.getUser().getUserName();

        if (auditStatus == 2){
            info += "???"+userName+"?????????";
        }else {
            info += "???"+userName+"????????????";
        }

        carPackageAudit.setInfo(carPackageAudit.getInfo() + "<br>" + info);
        carPackageAudit.setAuditTime(new Date());
        //???????????????????????????????????????
        Task task = taskService.createTaskQuery().processInstanceId(carPackageAudit.getInstanceId()).singleResult();
        //??????????????????
        taskService.setVariable(task.getId(),"auditStatus",auditStatus);
        //?????????????????????
        taskService.complete(task.getId());
        //??????????????????
        Task taskNext = taskService.createTaskQuery().processInstanceId(carPackageAudit.getInstanceId()).singleResult();
        //????????????
        //????????????---
        if (auditStatus == CarPackageAudit.STATUS_PASS){
            //1>???????????????????????????
            if (taskNext != null){
                //????????????????????????????????????-(??????)
                carPackageAudit.setAuditorId(Long.parseLong(task.getAssignee()));
            }else {
                //2>???????????????????????????- ????????????
                //?????????????????????????????????
                serviceItemService.updateByAuditStatus(carPackageAudit.getServiceItemId(),ServiceItem.AUDITSTATUS_APPROVED);
                carPackageAudit.setStatus(CarPackageAudit.STATUS_PASS);
            }
        }else {
            //????????????----
            //????????????????????????????????????  --- ?????????????????????????????????????????????????????? ?????????????????????2???????????????
            serviceItemService.updateByAuditStatus(carPackageAudit.getServiceItemId(),ServiceItem.AUDITSTATUS_INIT);
            carPackageAudit.setStatus(CarPackageAudit.STATUS_REJECT);
        }
        carPackageAuditMapper.updateByPrimaryKey(carPackageAudit);
    }
}
