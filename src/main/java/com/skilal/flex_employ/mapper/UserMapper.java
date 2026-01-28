package com.skilal.flex_employ.mapper;

import com.skilal.flex_employ.entity.User;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface UserMapper {

        @Select("<script>" +
                        "SELECT * FROM user WHERE 1=1 " +
                        "<if test='account != null and account != \"\"'> AND account LIKE CONCAT('%', #{account}, '%') </if>"
                        +
                        "<if test='role != null and role != \"\"'> AND role = #{role} </if>" +
                        "<if test='accountStatus != null'> AND account_status = #{accountStatus} </if>" +
                        "ORDER BY created_at DESC" +
                        "</script>")
        List<User> findAll(@Param("account") String account,
                        @Param("role") String role,
                        @Param("accountStatus") Integer accountStatus);

        @Select("SELECT * FROM user WHERE account = #{account}")
        User findByAccount(String account);

        @Select("SELECT * FROM user WHERE user_id = #{userId}")
        User findById(Long userId);

        @Insert("INSERT INTO user (account, password, gender, birth_date, phone, bank_card, role, account_status) " +
                        "VALUES (#{account}, #{password}, #{gender}, #{birthDate}, #{phone}, #{bankCard}, #{role}, #{accountStatus})")
        @Options(useGeneratedKeys = true, keyProperty = "userId")
        int insert(User user);

        @Update("<script>" +
                        "UPDATE user SET " +
                        "account = #{account}, " +
                        "<if test='password != null and password != \"\"'> password = #{password}, </if>" +
                        "gender = #{gender}, birth_date = #{birthDate}, phone = #{phone}, " +
                        "bank_card = #{bankCard}, role = #{role}, account_status = #{accountStatus} " +
                        "WHERE user_id = #{userId}" +
                        "</script>")
        int update(User user);

        @Update("UPDATE user SET password = #{password} WHERE user_id = #{userId}")
        int updatePassword(@Param("userId") Long userId, @Param("password") String password);

        @Delete("DELETE FROM user WHERE user_id = #{userId}")
        int delete(Long userId);
}
