package com.github.hetianyi.plugin.rrl;

import com.auxxs.base.common.util.StringUtil;

public class ClassInitializeFactory {

    public static <T> T initialize(String className, Class<T> superClass, Class<? extends T> fallbackClass)
            throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Class generatorClass;
        if (!StringUtil.isNullOrEmptyTrimed(className)) {
            generatorClass = Class.forName(className);
        } else {
            generatorClass = fallbackClass;
        }
        synchronized (ClassInitializeFactory.class) {
            if (superClass.isAssignableFrom(generatorClass)) {
                return (T) generatorClass.newInstance();
            } else {
                throw new IllegalArgumentException("class \"" + generatorClass.getName()
                        + "\" must be assignable to class \"" + superClass.getName() + "\"");
            }
        }
    }
}
