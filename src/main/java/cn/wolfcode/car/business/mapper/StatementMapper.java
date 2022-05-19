package cn.wolfcode.car.business.mapper;

import cn.wolfcode.car.business.domain.Statement;
import cn.wolfcode.car.business.query.StatementQuery;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

public interface StatementMapper {
    int deleteByPrimaryKey(Long id);

    int insert(Statement record);

    Statement selectByPrimaryKey(Long id);

    List<Statement> selectAll();

    int updateByPrimaryKey(Statement record);

    List<Statement> selectForList(StatementQuery qo);

    Statement selectByStatementId(Long statementId);

    int updateAmount(@Param("statementId") Long statementId, @Param("itemPrice") BigDecimal itemPrice, @Param("totalAmount") BigDecimal totalAmount, @Param("totalCount") BigDecimal totalCount);

    int updatePayStatement(Statement statement);

    Statement selectByAppointmentId(Long id);
}