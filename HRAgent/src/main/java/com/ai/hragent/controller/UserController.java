package com.ai.hragent.controller;


import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import com.ai.hragent.entity.User;
import com.ai.hragent.service.IUserService;
import com.ai.hragent.tools.Result;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestController
public class UserController {

    private static final String accessToken = "accessToken";
    @Autowired
    private IUserService userService;

    @Value("${jwt.secret:hragentapi}")
    private String secret;
    @Autowired
    private RestClientAutoConfiguration restClientAutoConfiguration;

    @PostMapping("/login")
    public Result login(@RequestBody User user) {
        // 非空验证
        if (user.getUsername() == null || user.getPassword() == null) {
            return Result.fail(400, "用户名或密码不能为空");
        }
        // 用户名查询用户对象
        User dbUser = userService.getOne(
                new QueryWrapper<User>().eq("username", user.getUsername())
        );
        if (dbUser == null)
            return Result.fail(400, "用户不存在");
        // todo：文明密码加密
        if (!dbUser.getPassword().equals(user.getPassword()))
            return Result.fail(400, "密码错误");
        // 验证成功
        Map<String, Object> playload = new HashMap();
        playload.put("uid", dbUser.getId());
        playload.put("username", dbUser.getUsername());
        playload.put("avatar", dbUser.getAvatar());
        String token = JWTUtil.createToken(playload, secret.getBytes());
        return Result.success(
                new HashMap<String, String>() {{
                    put(accessToken, token);
                }}
        );
    }

    @PostMapping("/userinfo")
    public Result userinfo(@RequestBody Map<String, String> params) {
        String token = params.get(accessToken);
        System.out.println(token);
        if (token == null) {
            return Result.fail(401, "请先登录");
        }
        // 验证 token 有效性
        if (!JWTUtil.verify(token, secret.getBytes())) {
            return Result.fail(401, "请先登录");
        }
        // 获取 token 用户信息
        JWT jwt = JWTUtil.parseToken(token);
        String username = (String) jwt.getPayload("username");
        String avatar = (String) jwt.getPayload("avatar");
        // 返回结果
        HashMap<String, Object> data = new HashMap<>();
        data.put("username", username);
        data.put("avatar", avatar);
        // todo:查询用户的角色进行设置
        data.put("permissions", new Object[]{"admin"}); // 一个用户拥有多个角色，必须返回数组
        return Result.success(data);
    }

    /**
     * 退出登录
     *
     * @return
     */
    @RequestMapping("/logout")
    public Result logout() {
        // todo:清空JWT信息
        return Result.success("success");
    }

    /**
     * 查询用户列表
     *
     * @param params
     * @return
     */
    @PostMapping("/user/getlist")
    public Result getList(@RequestBody Map<String, Object> params) {
        int pageNo = Integer.parseInt(params.get("pageNo").toString());
        pageNo = (pageNo == 0 ? 1 : pageNo); // 设置默认值
        int pageSize = Integer.parseInt(params.get("pageSize").toString());
        pageSize = (pageSize == 0 ? 10 : pageSize); // 设置默认值
        String username = params.get("username") == null ?
                null : params.get("username").toString();
        Page page = new Page(pageNo, pageSize);
        Page<User> result = null;
        if (username != null && !username.isEmpty()) {
            result = userService.page(page,
                    new QueryWrapper<User>().like("username", username)
            );
        } else {
            result = userService.page(page);
        }
        return Result.success(Map.of("list", result.getRecords(),
                "totalCount", result.getTotal()));
    }

    /**
     * 删除（一条或多条）
     */
    @PostMapping("/user/delete")
    public Result delete(@RequestBody Map<String, Object> params) {
        String idsStr = params.get("ids").toString();
        String[] ids = idsStr.split(",");
        boolean result = userService.removeByIds(Arrays.asList(ids));
        if (result)
            return Result.success("删除成功");
        else
            return Result.fail(500, "删除失败");
    }

    /**
     * 新增 OR 修改
     */
    @PostMapping("/user/edit")
    public Result edit(@RequestBody User user) {
        // todo:密码加密
        boolean result = userService.saveOrUpdate(user);
        if (result)
            return Result.success("保存成功");
        else
            return Result.fail(500, "保存失败");
    }

}
