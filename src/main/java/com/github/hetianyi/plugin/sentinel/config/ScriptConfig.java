package com.github.hetianyi.plugin.sentinel.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scripting.ScriptSource;
import org.springframework.scripting.support.ResourceScriptSource;

import java.io.IOException;


@Configuration
public class ScriptConfig {

    @Bean
    @Qualifier("scriptLimiter")
    public RedisScript<Long> scriptClientLimiter() throws IOException {
        ScriptSource scriptSource = new ResourceScriptSource(new ClassPathResource("META-INF/rate-limiter.lua"));
        return RedisScript.of(scriptSource.getScriptAsString(), Long.class);
    }

    /*@Bean
    @Qualifier("scriptGlobalLimiter")
    public RedisScript<Long> scriptGlobalLimiter() throws IOException {
        ScriptSource scriptSource = new ResourceScriptSource(new ClassPathResource("META-INF/global-rate-limiter.lua"));
        return RedisScript.of(scriptSource.getScriptAsString(), Long.class);
    }*/

    @Bean
    @Qualifier("scriptIncreaseFailTimes")
    public RedisScript<Boolean> scriptIncreaseFailTimes() throws IOException {
        ScriptSource scriptSource = new ResourceScriptSource(new ClassPathResource("META-INF/update-err-times-and-rt.lua"));
        return RedisScript.of(scriptSource.getScriptAsString(), Boolean.class);
    }

}
