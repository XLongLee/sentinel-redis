package com.github.hetianyi.plugin.rrl.util;

import com.github.hetianyi.common.util.AssertUtil;
import com.github.hetianyi.plugin.rrl.config.LimitResourceProperties;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConfigPropertiesCheckingUtil {

    public static final void checkConfig(LimitResourceProperties properties) {

        AssertUtil.notNull(properties, "properties must not be null");

        if (properties.getPeriod() <= 0) {
            throw new IllegalArgumentException("invalid config property value "+
                    "\"limit.config.period\": \"" + properties.getPeriod() +
                    "\", must be a positive int number");
        }

        if (properties.getPeriod() < 60) {
            log.warn("period less than 60 is not recommended");
        }

        if (properties.getCount() <= 0) {
            throw new IllegalArgumentException("invalid config property value "+
                    "\"limit.config.count\": \"" + properties.getPeriod() +
                    "\", must be a positive int number");
        }

        if (!properties.isEnableDegrade()) {
            log.debug("degrade is disabled");
            return;
        }
        if (properties.getPeriod() < 0) {
            throw new IllegalArgumentException("invalid config property value "+
                    "\"limit.config.degradeValue\": \"" + properties.getDegradeValue() +
                    "\", must be a positive int number when " +
                    "\"limit.config.degradeStrategy\" is \"" + properties.getDegradeStrategy() +"\"");
        }

        properties.setDegradeStrategy(properties.getDegradeStrategy().toUpperCase());

        if (LimitResourceProperties.DEGRADE_RT.equals(properties.getDegradeStrategy())
                || LimitResourceProperties.DEGRADE_EC.equals(properties.getDegradeStrategy())) {
            try {
                int d = Integer.parseInt(properties.getDegradeValue());
                if (d <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("invalid config property value "+
                        "\"limit.config.degradeValue\": \"" + properties.getDegradeValue() +
                        "\", must be a positive int number when " +
                        "\"limit.config.degradeStrategy\" is \"" + properties.getDegradeStrategy() +"\"");
            }
        }
        else if (LimitResourceProperties.DEGRADE_EP.equals(properties.getDegradeStrategy())) {
            try {
                double d = Double.parseDouble(properties.getDegradeValue());
                if (d <= 0 || d > 1) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("invalid config property value " +
                        "\"limit.config.degradeValue\": \""+ properties.getDegradeValue() +
                        "\", must be a decimals between 0 and 1 when " +
                        "\"limit.config.degradeStrategy\" is \"" + LimitResourceProperties.DEGRADE_EP +"\"");
            }
        }
        else {
            throw new IllegalArgumentException("invalid config property value " +
                    "\"limit.config.degradeStrategy\": \"" + properties.getDegradeStrategy() +
                    "\", must be one of \"RT\", \"EC\", \"EP\"");
        }
    }


    public static final boolean isValidDegradeStrategyValue(String degradeStrategy, String degradeValue) {
        if (LimitResourceProperties.DEGRADE_RT.equals(degradeStrategy)
                || LimitResourceProperties.DEGRADE_EC.equals(degradeStrategy)) {
            try {
                int d = Integer.parseInt(degradeValue);
                return d > 0;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        else if (LimitResourceProperties.DEGRADE_EP.equals(degradeStrategy)) {
            try {
                double d = Double.parseDouble(degradeValue);
                return (d > 0 && d <= 1);
            } catch (NumberFormatException e) {
                return false;
            }
        }
        else {
            return false;
        }
    }
}
