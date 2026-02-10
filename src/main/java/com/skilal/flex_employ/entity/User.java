package com.skilal.flex_employ.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class User {
    private Long userId;
    private String account;
    private String name;
    private String password;
    private String gender;
    private LocalDate birthDate;
    private String phone;
    private String bankCard;
    private String role;
    private Integer accountStatus;
    private LocalDateTime createdAt;
}
