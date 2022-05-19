package cn.wolfcode.car.business.service;

import cn.wolfcode.car.business.domain.BpmnInfo;
import cn.wolfcode.car.business.query.BpmnInfoQuery;
import cn.wolfcode.car.common.base.page.TablePageInfo;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

/**
 * 审核服务接口
 */
public interface IBpmnInfoService {

    /**
     * 分页
     * @param qo
     * @return
     */
    TablePageInfo<BpmnInfo> query(BpmnInfoQuery qo);


    /**
     * 查单个
     * @param id
     * @return
     */
    BpmnInfo get(Long id);


    /**
     * 保存
     * @param bpmnInfo
     */
    void save(BpmnInfo bpmnInfo);

  
    /**
     * 更新
     * @param bpmnInfo
     */
    void update(BpmnInfo bpmnInfo);

    /**
     *  批量删除
     * @param id
     */
    void deleteBatch(Long id);

    /**
     * 查询全部
     * @return
     */
    List<BpmnInfo> list();


    String saveUploadFile(MultipartFile file);


    void saveDeploy(BpmnInfo bpmnInfo) throws IOException;

    void updateEditPage(BpmnInfo bpmnInfo);

    InputStream getReadResource(String deploymentId, String type);

    BpmnInfo selectByBpmnType(String car_package);
}
