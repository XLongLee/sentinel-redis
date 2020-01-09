package com.github.hetianyi.plugin.rrl;


import com.github.hetianyi.plugin.rrl.config.AnnotatedLimitResourceProperties;

/**
 *
 */
public class PropertiesFactory {

    private PropertiesFactory() {
    }

    /**
     * creates LimitResourceProperties.
     * @param period
     * @param count
     * @param degradeStrategy
     * @param degradeValue
     * @param degradeWindow
     * @param enableDegrade
     * @return
     */
    public static final AnnotatedLimitResourceProperties create(int period,
                                                                int count,
                                                                String degradeStrategy,
                                                                String degradeValue,
                                                                int degradeWindow,
                                                                boolean enableDegrade) {
        return new AnnotatedLimitResourceProperties(period, count, degradeStrategy, degradeValue, degradeWindow,
                enableDegrade);
    }
}