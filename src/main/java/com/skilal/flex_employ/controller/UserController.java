package com.skilal.flex_employ.controller;

import com.skilal.flex_employ.common.Result;
import com.skilal.flex_employ.entity.User;
import com.skilal.flex_employ.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/{id}")
    public Result<User> getUserInfo(@PathVariable Long id) {
        User user = userMapper.findById(id);
        user.setPassword(null); // 不返回密码
        return Result.success(user);
    }

    @PutMapping("/{id}")
    public Result<String> updateUserInfo(@PathVariable Long id, @RequestBody User user) {
        user.setUserId(id);
        int result = userMapper.update(user);
        if (result > 0) {
            return Result.success("更新成功");
        }
        return Result.error("更新失败");
    }

    @PutMapping("/{id}/password")
    public Result<String> updatePassword(@PathVariable Long id, @RequestBody Map<String, String> data) {
        String oldPassword = data.get("oldPassword");
        String newPassword = data.get("newPassword");

        User user = userMapper.findById(id);
        if (!user.getPassword().equals(oldPassword)) {
            return Result.error("原密码错误");
        }

        int result = userMapper.updatePassword(id, newPassword);
        if (result > 0) {
            return Result.success("密码修改成功");
        }
        return Result.error("密码修改失败");
    }
}
