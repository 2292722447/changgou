package com.changgou.user.feign;

import com.changgou.user.pojo.User;
import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user")
@RequestMapping("/user")
public interface UserFeign {




    /**
     * 添加用户积分
     */
    @GetMapping("/points/add")
     Result addPoints(@RequestParam Integer points);

    /**
     * 根据id查询用户信息
     * @param id
     * @return
     */
    @GetMapping({"/load/{id}"})
    Result<User> findById(@PathVariable String id);




}
