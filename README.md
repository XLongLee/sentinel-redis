# sentinel-redis

基于Redis实现的分布式限流和降级插件。



##### 引入maven依赖：

```xml
<dependency>
    <groupId>com.github.hetianyi</groupId>
    <artifactId>sentinel-redis</artifactId>
    <version>1.0.0</version>
</dependency>
```



##### Redis集群搭建

> 此处使用Docker Swarm搭建redis集群。也可使用单机Redis。

点击此处查看[编排文件](docker-compose/docker-compose-redis-cluster.yml)

```shell
docker stack deploy -c docker-compose-redis-cluster.yml redis
docker exec -it <containerId> redis-cli \
--cluster create \
192.168.245.142:10001 \
192.168.245.142:10001 \
192.168.245.142:10001 \
--cluster-replicas 0
```



##### 基础配置：

```yaml
# redis配置
redis:
  cluster:
    nodes:
      - "192.168.241.133:10001"
      - "192.168.241.134:10001"
      - "192.168.241.135:10001"
    max-redirects: 10

# 限流降级配置
limit:
  config:
    period: 60 # 周期
    count: 60 # 一个周期内允许每个资源的调用量
    enable-degrade: true # 是否开启降级功能
    degrade-strategy: EC # 降级策略
    degrade-value: 3 # 触发降级条件
    degrade-window: 5 # 降级窗口
```

> 更多配置，参考：com.auxxs.base.limiter.config.LimitResourceProperties



##### 使用

```java
@RestController
@RequestMapping("/v1")
@Slf4j
public class LimitTestCtrl {
    
    @RequestMapping("/user")
    @LimitResource
    public RestResponse<Boolean> user() {
        return RestResponse.success();
    }

    @RequestMapping("/ticket")
    @LimitResource(
            blockHandler = MyBlockHandler.class,
            fallbackHandler = MyFallbackHandler.class,
            period = 60,
            count = 1,
            enableDegrade = true,
            degradeStrategy = "EC",
            degradeValue = "1",
            degradeWindow = 5
    )
    public RestResponse<Boolean> ticket() {
        return RestResponse.success();
    }
}
```

