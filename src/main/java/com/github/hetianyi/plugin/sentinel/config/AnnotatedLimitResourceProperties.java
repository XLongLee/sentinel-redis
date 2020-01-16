package com.github.hetianyi.plugin.sentinel.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnnotatedLimitResourceProperties {

    /**
     * 限制周期（单位s），
     * 建议不小于60
     */
    private int period = 0;

    /**
     * 一个周期内对每个API调用限制总次数
     * 换算成每秒为：count/period，
     * 在服务端会转换为每秒最大调用次数。
     */
    private int count = 0;

    /**
     * 降级策略 <br/>
     * - RT: 平均响应时间 <br/>
     * 统计近一分钟内的接口平均响应时间，如果超过阈值，
     * 则进入快速失败期，直到近一分钟内的平均响应时间小于阈值。<br/>
     * - EP: 异常比例 <br/>
     * 统计近一分钟内的接口失败次数占总调用次数的比例，如果超过阈值，
     * 则进入快速失败期，直到近一分钟内的失败比例小于阈值。<br/>
     * - EC: 异常数 <br/>
     * 统计近一分钟内的接口失败次数，如果超过阈值，则进入快速失败期，
     * 直到近一分钟内的失败次数小于阈值。
     */
    private String degradeStrategy;

    /**
     * 降级策略对应的值，根据degradeStrategy的不同，此处对应的含义也不通。
     * RT: 平均响应时间（ms）
     * EP: 异常比例（0.0-1.0）
     * EC: 一分钟内的异常数（int）
     */
    private String degradeValue;

    /**
     * 降级窗口时间(单位s)，不能大于60
     */
    private int degradeWindow = 5;

    /**
     * 是否开启降级功能
     */
    private boolean enableDegrade = true;
}
