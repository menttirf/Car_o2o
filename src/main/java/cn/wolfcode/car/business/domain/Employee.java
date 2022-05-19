package cn.wolfcode.car.business.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Setter
@Getter
public class Employee {
    private Long id;

    private String name;

    private String email;

    private Integer age;

    private Integer admin;

    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private Date hiredate;

    private Department department;
}