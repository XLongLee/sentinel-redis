
-- redis.log(redis.LOG_WARNING, 'start...')
local key = KEYS[1]

local period = tonumber(ARGV[1])
local count = tonumber(ARGV[2])
-- 每秒钟产生的令牌数
local tickets_per_sec = count / period
-- 请求的令牌数
local required_tickets = tonumber(ARGV[3])
-- 快速失败阈值
local enale_degrade = ARGV[4]
local degrade_strategy = ARGV[5]
local degrade_threshold = tonumber(ARGV[6])
-- 快速失败窗口时间
local degrade_window = tonumber(ARGV[7])
-- 添加令牌的时间间隔
local stable_interval_micros = 1000000 / tickets_per_sec




-- 当前时间
local time = redis.call('time')
local now_micros = tonumber(time[1]) * 1000000 + tonumber(time[2])


-- 判断是否进入快速失败期
local function isFailFast(key, now_micros, degrade_strategy, degrade_threshold)

    local fail_fast_ts = tonumber(redis.call('hget', key, 'fail_fast_ts') or 0)

    if (now_micros < fail_fast_ts) then
        redis.log(redis.LOG_WARNING, 'in fail fast')
        return true, fail_fast_ts
    end

    -- ------------------------------------------------------------------------------- 以下是快速失败检查
    if (degrade_strategy == 'EC' or degrade_strategy == 'EP') then
        -- 获取一分钟内错误次数  ec : error count
        local error_count_list = redis.call('hmget', key,
            '1ec', '2ec', '3ec', '4ec', '5ec', '6ec', '7ec', '8ec', '9ec',
            '10ec', '11ec', '12ec', '13ec', '14ec', '15ec', '16ec', '17ec', '18ec', '19ec',
            '20ec', '21ec', '22ec', '23ec', '24ec', '25ec', '26ec', '27ec', '28ec', '29ec',
            '30ec', '31ec', '32ec', '33ec', '34ec', '35ec', '36ec', '37ec', '38ec', '39ec',
            '40ec', '41ec', '42ec', '43ec', '44ec', '45ec', '46ec', '47ec', '48ec', '49ec',
            '50ec', '51ec', '52ec', '53ec', '54ec', '55ec', '56ec', '57ec', '58ec', '59ec', '60ec')
        -- 对应最后一分钟内错误更新时间  ect : ec + timestamp
        local error_count_last_update_list = redis.call('hmget', key,
            '1ect', '2ect', '3ect', '4ect', '5ect', '6ect', '7ect', '8ect', '9ect',
            '10ect', '11ect', '12ect', '13ect', '14ect', '15ect', '16ect', '17ect', '18ect', '19ect',
            '20ect', '21ect', '22ect', '23ect', '24ect', '25ect', '26ect', '27ect', '28ect', '29ect',
            '30ect', '31ect', '32ect', '33ect', '34ect', '35ect', '36ect', '37ect', '38ect', '39ect',
            '40ect', '41ect', '42ect', '43ect', '44ect', '45ect', '46ect', '47ect', '48ect', '49ect',
            '50ect', '51ect', '52ect', '53ect', '54ect', '55ect', '56ect', '57ect', '58ect', '59ect', '60ect')
        -- 最近一分钟内所有请求数量
        local all_calls = {}
        local all_calls_last_update_list = {}
        if (degrade_strategy == 'EP') then
            all_calls = redis.call('hmget', key,
                '1ac', '2ac', '3ac', '4ac', '5ac', '6ac', '7ac', '8ac', '9ac',
                '10ac', '11ac', '12ac', '13ac', '14ac', '15ac', '16ac', '17ac', '18ac', '19ac',
                '20ac', '21ac', '22ac', '23ac', '24ac', '25ac', '26ac', '27ac', '28ac', '29ac',
                '30ac', '31ac', '32ac', '33ac', '34ac', '35ac', '36ac', '37ac', '38ac', '39ac',
                '40ac', '41ac', '42ac', '43ac', '44ac', '45ac', '46ac', '47ac', '48ac', '49ac',
                '50ac', '51ac', '52ac', '53ac', '54ac', '55ac', '56ac', '57ac', '58ac', '59ac', '60ac')
            all_calls_last_update_list = redis.call('hmget', key,
                '1act', '2act', '3act', '4act', '5act', '6act', '7act', '8act', '9act',
                '10act', '11act', '12act', '13act', '14act', '15act', '16act', '17act', '18act', '19act',
                '20act', '21act', '22act', '23act', '24act', '25act', '26act', '27act', '28act', '29act',
                '30act', '31act', '32act', '33act', '34act', '35act', '36act', '37act', '38act', '39act',
                '40act', '41act', '42act', '43act', '44act', '45act', '46act', '47act', '48act', '49act',
                '50act', '51act', '52act', '53act', '54act', '55act', '56act', '57act', '58act', '59act', '60act')
        end

        local total_error_count = 0
        local total_call_count = 0
        for k, v in ipairs(error_count_list) do
            if (now_micros - tonumber(error_count_last_update_list[k] or now_micros) < 60000000) then
                total_error_count = total_error_count + tonumber(v or 0)
            end
            if (degrade_strategy == 'EP'
                    and (now_micros - tonumber(all_calls_last_update_list[k] or now_micros) < 60000000)) then
                total_call_count = total_call_count + tonumber(all_calls[k] or 0)
            end
        end

        --redis.log(redis.LOG_WARNING, total_error_count)
        --redis.log(redis.LOG_WARNING, total_call_count)
        --redis.log(redis.LOG_WARNING, total_error_count/total_call_count)


        if (degrade_strategy == 'EC' and total_error_count >= degrade_threshold) then
            return true, 0
        elseif (degrade_strategy == 'EP' and total_call_count > 0
                and total_error_count/total_call_count >= degrade_threshold) then
            return true, 0
        end
        return false, 0
    else
        -- 获取一分钟内平均响应时间
        local rt_list = redis.call('hmget', key,
            '1rt', '2rt', '3rt', '4rt', '5rt', '6rt', '7rt', '8rt', '9rt',
            '10rt', '11rt', '12rt', '13rt', '14rt', '15rt', '16rt', '17rt', '18rt', '19rt',
            '20rt', '21rt', '22rt', '23rt', '24rt', '25rt', '26rt', '27rt', '28rt', '29rt',
            '30rt', '31rt', '32rt', '33rt', '34rt', '35rt', '36rt', '37rt', '38rt', '39rt',
            '40rt', '41rt', '42rt', '43rt', '44rt', '45rt', '46rt', '47rt', '48rt', '49rt',
            '50rt', '51rt', '52rt', '53rt', '54rt', '55rt', '56rt', '57rt', '58rt', '59rt', '60rt')
        -- 对应最后一分钟内平均响应时间更新时间  ect : ec + timestamp
        local rt_last_update_list = redis.call('hmget', key,
            '1rtt', '2rtt', '3rtt', '4rtt', '5rtt', '6rtt', '7rtt', '8rtt', '9rtt',
            '10rtt', '11rtt', '12rtt', '13rtt', '14rtt', '15rtt', '16rtt', '17rtt', '18rtt', '19rtt',
            '20rtt', '21rtt', '22rtt', '23rtt', '24rtt', '25rtt', '26rtt', '27rtt', '28rtt', '29rtt',
            '30rtt', '31rtt', '32rtt', '33rtt', '34rtt', '35rtt', '36rtt', '37rtt', '38rtt', '39rtt',
            '40rtt', '41rtt', '42rtt', '43rtt', '44rtt', '45rtt', '46rtt', '47rtt', '48rtt', '49rtt',
            '50rtt', '51rtt', '52rtt', '53rtt', '54rtt', '55rtt', '56rtt', '57rtt', '58rtt', '59rtt', '60rtt')

        local total_rt = 0
        local valid_count = 0
        for k, v in ipairs(rt_list) do
            if (now_micros - tonumber(rt_last_update_list[k] or 0) < 60000000) then
                total_rt = total_rt + tonumber(v or 0)
                valid_count = valid_count + 1
            end
        end
        -- 计算平均响应时间（ms）
        local avg_rt = total_rt / valid_count

        -- redis.log(redis.LOG_WARNING, avg_rt)

        redis.log(redis.LOG_WARNING, 'rt----')
        redis.log(redis.LOG_WARNING, avg_rt)

        if (avg_rt > degrade_threshold) then
             return true, 0
        end
        return false, 0
    end
end

-- 快速失败检查
if (enale_degrade == 'true') then
    local is_fail_fast, next_micro = isFailFast(key, now_micros, degrade_strategy, degrade_threshold)
    if (is_fail_fast) then
        if (now_micros > next_micro) then
            -- 执行命令
            redis.replicate_commands()
            redis.call('hset', key, 'fail_fast_ts', now_micros + degrade_window * 1000000)
            redis.call('expire', key, math.max(period, degrade_window))
        end
        return 3
    end
end







-- ------------------------------------------------------------------------------- 以下是令牌操作

-- client
local next_free_ticket_micro = tonumber(redis.call('hget', key, 'next_free_ticket_micro') or 0)
local stored_tickets = tonumber(redis.call('hget', key, 'stored_tickets') or count)
-- 添加令牌
if (now_micros > next_free_ticket_micro) then
    -- redis.log(redis.LOG_WARNING, 'add ticket')
    local new_tickets = (now_micros - next_free_ticket_micro) / stable_interval_micros
    stored_tickets = math.min(count, stored_tickets + new_tickets)
    next_free_ticket_micro = now_micros
end

if (stored_tickets < required_tickets) then
    -- redis.log(redis.LOG_WARNING, 'short of ticket : fail_times : ', fail_times)
    -- 令牌不足
    return 1
end

local sec_slot = tonumber(time[1]) % 60 + 1
local hkey1 = tostring(sec_slot)..'ac'
local hkey2 = tostring(sec_slot)..'act'
-- 获取该节点总异常数
local ac = tonumber(redis.call('hget', key, hkey1) or 0)
-- 获取该slot上次更新时间
local lt = tonumber(redis.call('hget', key, hkey2) or 0)

-- 使用59s防止临界问题
if (now_micros - lt > 59000000) then
    -- 已过期
    ac = 1
else
    ac = ac + 1
end

-- 执行命令
redis.replicate_commands()
redis.call('hset', key, 'stored_tickets', stored_tickets - required_tickets)
redis.call('hset', key, 'next_free_ticket_micro', next_free_ticket_micro)
redis.call('hset', key, 'next_free_ticket_micro', next_free_ticket_micro)
redis.call('hset', key, hkey1, ac)
redis.call('hset', key, hkey2, now_micros)
redis.call('expire', key, period)

-- 成功
return 0