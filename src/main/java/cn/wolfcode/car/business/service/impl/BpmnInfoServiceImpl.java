package cn.wolfcode.car.business.service.impl;

;
import cn.wolfcode.car.business.domain.BpmnInfo;
import cn.wolfcode.car.business.mapper.BpmnInfoMapper;
import cn.wolfcode.car.business.query.BpmnInfoQuery;
import cn.wolfcode.car.business.service.IBpmnInfoService;
import cn.wolfcode.car.common.base.page.TablePageInfo;
import cn.wolfcode.car.common.config.SystemConfig;
import cn.wolfcode.car.common.exception.BusinessException;
import cn.wolfcode.car.common.util.file.FileUploadUtils;
import com.github.pagehelper.PageHelper;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.*;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.image.ProcessDiagramGenerator;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipInputStream;

@Service
@Transactional
public class BpmnInfoServiceImpl implements IBpmnInfoService {

    @Autowired
    private BpmnInfoMapper bpmnInfoMapper;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;


    @Override
    public TablePageInfo<BpmnInfo> query(BpmnInfoQuery qo) {
        PageHelper.startPage(qo.getPageNum(), qo.getPageSize());
        return new TablePageInfo<BpmnInfo>(bpmnInfoMapper.selectForList(qo));
    }


    //添加
    @Override
    public void save(BpmnInfo bpmnInfo) {
        bpmnInfoMapper.insert(bpmnInfo);
    }

    @Override
    public BpmnInfo get(Long id) {
        return bpmnInfoMapper.selectByPrimaryKey(id);
    }


    //编辑
    @Override
    public void update(BpmnInfo bpmnInfo) {
        bpmnInfoMapper.updateByPrimaryKey(bpmnInfo);
    }

    //删除
    @Override
    public void deleteBatch(Long id) {
        //将文件删除
        BpmnInfo bpmnInfo = bpmnInfoMapper.selectByPrimaryKey(id);
        File file = new File(SystemConfig.getProfile(), bpmnInfo.getBpmnPath());
        if (file.exists()) {
            file.delete();
        }

        //通过唯一标识删除流程定义
        List<ProcessInstance> list = runtimeService.createProcessInstanceQuery()
                .processDefinitionKey(bpmnInfo.getActProcessKey())
                .list();

        for (ProcessInstance instance : list) {
            //
        }

        //删除流程定义
        repositoryService.deleteDeployment(bpmnInfo.getDeploymentId(), true);

        //删除info表中对应的数据
        bpmnInfoMapper.deleteByPrimaryKey(id);
    }

    @Override
    public List<BpmnInfo> list() {
        return bpmnInfoMapper.selectAll();
    }

    //保存上传文件操作
    @Override
    public String saveUploadFile(MultipartFile file) {
        //保存文件
        //先判断是否存在文件
        String path = null;
        if (file != null && file.getSize() > 0) {
            //获取文件后缀
            String ext = FilenameUtils.getExtension(file.getOriginalFilename());
            //判断文件类型，只能为zip bpmn为后缀的文件
            if ("zip".equalsIgnoreCase(ext) || "bpmn".equalsIgnoreCase(ext)) {
                try {
                    path = FileUploadUtils.upload(SystemConfig.getUploadPath(), file);
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new BusinessException("文件错误");
                }
            } else {
                throw new BusinessException("文件格式错误以zip,bpmn结尾");
            }
        } else {
            throw new BusinessException("文件为空，请重新选择!");
        }
        return path;
    }

    @Override
    public void saveDeploy(BpmnInfo bpmnInfo) throws IOException {
        //获取文件后缀名
        String ext = FilenameUtils.getExtension(bpmnInfo.getBpmnPath());
        //获取文件，根据文件路径
        File file = new File(SystemConfig.getProfile(), bpmnInfo.getBpmnPath());
        //部署流程
        DeploymentBuilder deployment = repositoryService.createDeployment();

        //判断文件类型
        if ("zip".equalsIgnoreCase(ext)) {
            //zip
            deployment.addZipInputStream(new ZipInputStream(new FileInputStream(file)));
        } else if ("bpmn".equalsIgnoreCase(ext)) {
            //bpmn
            deployment.addInputStream(bpmnInfo.getBpmnPath(), new FileInputStream(file));
        } else {
            throw new BusinessException("文件类型错误");
        }
        //部署
        Deployment deploy = deployment.deploy();

        //流程定义
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deploy.getId())
                .singleResult();

        //对数据封装处理
        bpmnInfo.setDeploymentId(deploy.getId()); //流程部署id
        bpmnInfo.setActProcessId(processDefinition.getId()); //流程定义id
        bpmnInfo.setActProcessKey(processDefinition.getKey()); //流程定义key
        bpmnInfo.setBpmnName(processDefinition.getName()); //流程定义名称name
        bpmnInfo.setDeployTime(deploy.getDeploymentTime()); // 流程部署时间

        bpmnInfoMapper.insert(bpmnInfo);
    }


    @Override
    public void updateEditPage(BpmnInfo bpmnInfo) {
        bpmnInfoMapper.updateEditPage(bpmnInfo);
    }

    @Override
    public InputStream getReadResource(String deploymentId, String type) {
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                                            .deploymentId(deploymentId)
                                            .latestVersion()
                                            .singleResult();
        if ("xml".equalsIgnoreCase(type)) {
            return repositoryService.getResourceAsStream(deploymentId,
                    definition.getResourceName());
        } else if ("png".equalsIgnoreCase(type)) {
            BpmnModel model = repositoryService.getBpmnModel(definition.getId());
            ProcessDiagramGenerator generator = new
                    DefaultProcessDiagramGenerator();
            //generateDiagram(流程模型,需要高亮的节点,需要高亮的线条,后面三个参数都表示是字体)
            InputStream inputStream = generator.generateDiagram(model,
                    Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                    "宋体", "宋体", "宋体");
            return inputStream;
        }
        return null;
    }

    @Override
    public BpmnInfo selectByBpmnType(String carPackage) {
        return bpmnInfoMapper.selectByBpmnType(carPackage);
    }
}
