-- 更新错误数量和响应时间
--
local key = KEYS[1]
local error_count = tonumber(ARGV[1]) or 0
local new_rt = tonumber(ARGV[2]) or 0

-- 当前时间
local time = redis.call('time')
local now_micros = tonumber(time[1]) * 1000000 + tonumber(time[2])
local sec_slot = tonumber(time[1]) % 60 + 1


local function updateErrorCount(sec_slot, key, now_micros, error_count)
    local hkey1 = tostring(sec_slot)..'ec'
    local hkey2 = tostring(sec_slot)..'ect'
    -- 获取该节点总异常数
    local ec = tonumber(redis.call('hget', key, hkey1) or 0)
    -- 获取该slot上次更新时间
    local lt = tonumber(redis.call('hget', key, hkey2) or now_micros)

    -- 使用59s防止临界问题
    if (now_micros - lt > 59000000) then
        -- 已过期
        ec = 1
    else
        ec = ec + error_count
    end

    redis.replicate_commands()
    redis.call('hset', key, hkey1, ec)
    redis.call('hset', key, hkey2, now_micros)
    redis.call('expire', key, 60)
end


local function updateRT(sec_slot, key, now_micros, new_rt)
    local hkey1 = tostring(sec_slot)..'rt'
    local hkey2 = tostring(sec_slot)..'rtt'
    -- 获取该节点总异常数
    local rt = tonumber(redis.call('hget', key, hkey1) or new_rt)
    -- 获取该slot上次更新时间
    local rtt = tonumber(redis.call('hget', key, hkey2) or now_micros)

    -- 使用59s防止临界问题
    if (now_micros - rtt > 59000000) then
        -- 已过期
        rt = new_rt
    else
        rt = (rt + new_rt) / 2
    end

    --redis.log(redis.LOG_WARNING, 'new rt----')
    --redis.log(redis.LOG_WARNING, rt)

    redis.replicate_commands()
    redis.call('hset', key, hkey1, rt)
    redis.call('hset', key, hkey2, now_micros)
    redis.call('expire', key, 60)
end

if (error_count > 0) then
    updateErrorCount(sec_slot, key, now_micros, error_count)
end

updateRT(sec_slot, key, now_micros, new_rt)

return true