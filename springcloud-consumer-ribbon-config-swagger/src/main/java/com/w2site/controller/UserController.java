package com.w2site.controller;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.w2site.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by TR_VMHyper on 2019/5/4.
 */
@RequestMapping("user")
@RestController
public class UserController {
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private LoadBalancerClient loadBalancerClient;

    /**
     * @param id
     * @return
     */
    @GetMapping("{id}")
    @HystrixCommand(fallbackMethod = "userFallbackMethod")
    public User getUser(@PathVariable String id) {
        return this.restTemplate.getForObject("http://springcloud-provider-config/user/" + id, User.class);
    }

    public User userFallbackMethod(String id) {
        return null;
    }

    /**
     * 这块ribbon不支持复杂数据类型list，所以要用数组接受，然后转list
     *
     * @return
     */
    @GetMapping("list")
    @HystrixCommand(fallbackMethod = "userList")
    public List<User> users(HttpServletRequest request) {
        try {
            User[] forObject = this.restTemplate.getForObject("http://springcloud-provider-config/user/list", User[].class);
            List<User> users = Arrays.asList(forObject);
            return users == null ? new ArrayList<User>() : users;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<User> userList(HttpServletRequest request) {
        return null;
    }

    /**
     * 通过服务id获取服务的地址
     *
     * @return
     */
    @GetMapping("ribbon")
    public String ribbon() {
        ServiceInstance serviceInstance = loadBalancerClient.choose("springcloud-provider-config");
        return serviceInstance.getUri().toString();
    }
}
