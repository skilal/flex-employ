package com.skilal.flex_employ.entity;

import lombok.Data;

@Data
public class Company {
    private Long companyId;
    private String companyName;
    private String responsiblePerson;
    private String contactPhone;
    private Integer companyStatus; // 0-已关闭, 1-存续中
}
