-- KEYS[1] = rate:tb:{endpoint}:{userId}

local key = KEYS[1]

local capacity = tonumber(ARGV[1])
local refill_rate = tonumber(ARGV[2])
local now = tonumber(ARGV[3])

-- Read current state
local data = redis.call('HMGET', key, 'tokens', 'last_refill')

local tokens = tonumber(data[1]) or capacity
local last_refill = tonumber(data[2]) or now

-- Calculate elapsed time in seconds
local elapsed = (now - last_refill) / 1000.0

-- Refill bucket
local refilled = math.min(
    capacity,
    tokens + (elapsed * refill_rate)
)

-- Not enough tokens
if refilled < 1 then
    local missing_tokens = 1 - refilled
    local wait_seconds = missing_tokens / refill_rate
    local wait_ms = math.ceil(wait_seconds * 1000)

    return {0, wait_ms}
end

-- Consume one token
local new_tokens = refilled - 1

-- Persist new state
redis.call(
    'HMSET',
    key,
    'tokens',
    new_tokens,
    'last_refill',
    now
)

-- Allowed
return {1, math.floor(new_tokens)}