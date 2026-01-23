package com.skilal.flex_employ.mapper;

import com.skilal.flex_employ.entity.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM user WHERE account = #{account}")
    User findByAccount(String account);

    @Select("SELECT * FROM user WHERE user_id = #{userId}")
    User findById(Long userId);

    @Insert("INSERT INTO user (account, password, gender, birth_date, phone, bank_card, role, account_status) " +
            "VALUES (#{account}, #{password}, #{gender}, #{birthDate}, #{phone}, #{bankCard}, #{role}, #{accountStatus})")
    @Options(useGeneratedKeys = true, keyProperty = "userId")
    int insert(User user);

    @Update("UPDATE user SET gender = #{gender}, birth_date = #{birthDate}, phone = #{phone}, " +
            "bank_card = #{bankCard} WHERE user_id = #{userId}")
    int update(User user);

    @Update("UPDATE user SET password = #{password} WHERE user_id = #{userId}")
    int updatePassword(@Param("userId") Long userId, @Param("password") String password);
}
