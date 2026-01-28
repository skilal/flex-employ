package com.skilal.flex_employ.mapper;

import com.skilal.flex_employ.entity.Company;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface CompanyMapper {

        @Select("<script>" +
                        "SELECT * FROM company WHERE 1=1 " +
                        "<if test='companyName != null and companyName != \"\"'> AND company_name LIKE CONCAT('%', #{companyName}, '%') </if>"
                        +
                        "<if test='companyStatus != null'> AND company_status = #{companyStatus} </if>" +
                        "ORDER BY company_id DESC" +
                        "</script>")
        List<Company> findAll(@Param("companyName") String companyName,
                        @Param("companyStatus") Integer companyStatus);

        @Select("SELECT * FROM company WHERE company_id = #{companyId}")
        Company findById(Long companyId);

        @Insert("INSERT INTO company (company_name, responsible_person, contact_phone, company_status) " +
                        "VALUES (#{companyName}, #{responsiblePerson}, #{contactPhone}, #{companyStatus})")
        @Options(useGeneratedKeys = true, keyProperty = "companyId")
        int insert(Company company);

        @Update("UPDATE company SET company_name = #{companyName}, responsible_person = #{responsiblePerson}, " +
                        "contact_phone = #{contactPhone}, company_status = #{companyStatus} WHERE company_id = #{companyId}")
        int update(Company company);

        @Delete("DELETE FROM company WHERE company_id = #{companyId}")
        int delete(Long companyId);
}
