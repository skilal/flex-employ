package com.skilal.flex_employ.controller;

import com.skilal.flex_employ.common.Result;
import com.skilal.flex_employ.entity.User;
import com.skilal.flex_employ.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserMapper userMapper;

    // 获取用户列表（管理员）
    @GetMapping
    public Result<List<User>> getUsers(@RequestParam(required = false) String account,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer accountStatus) {
        List<User> users = userMapper.findAll(account, role, accountStatus);
        // 不返回密码
        users.forEach(user -> user.setPassword(null));
        return Result.success(users);
    }

    @GetMapping("/{id}")
    public Result<User> getUserInfo(@PathVariable Long id) {
        User user = userMapper.findById(id);
        user.setPassword(null); // 不返回密码
        return Result.success(user);
    }

    // 创建用户（管理员）
    @PostMapping
    public Result<String> createUser(@RequestBody User user) {
        // 检查账号是否已存在
        User existUser = userMapper.findByAccount(user.getAccount());
        if (existUser != null) {
            return Result.error("账号已存在");
        }

        int result = userMapper.insert(user);
        if (result > 0) {
            return Result.success("创建成功");
        }
        return Result.error("创建失败");
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

    // 删除用户（管理员）
    @DeleteMapping("/{id}")
    public Result<String> deleteUser(@PathVariable Long id) {
        int result = userMapper.delete(id);
        if (result > 0) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
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
