-- ============================================================================
-- Flash Sale Purchase Script
--
-- KEYS[1] -> inventory:{saleItemId}
-- KEYS[2] -> user_purchases:{saleItemId}:{userId}
--
-- ARGV[1] -> quantity
-- ARGV[2] -> maxPerUser
--
-- Return Codes
-- {-1, currentPurchased} : SOLD_OUT
-- {-2, currentPurchased} : PURCHASE_LIMIT_EXCEEDED
-- {-3, 0}               : INVENTORY_NOT_LOADED
-- {remainingStock, newUserPurchaseCount} : SUCCESS
-- ============================================================================

local inventoryKey = KEYS[1]
local userPurchaseKey = KEYS[2]

local quantity = tonumber(ARGV[1])
local maxPerUser = tonumber(ARGV[2])

--------------------------------------------------------------------
-- Check inventory
--------------------------------------------------------------------
local currentInventory = tonumber(redis.call("GET", inventoryKey))

-- Inventory was never loaded into Redis
if currentInventory == nil then
    return {-3, 0}
end

-- Inventory exhausted
if currentInventory < quantity then
    local purchased = tonumber(redis.call("HGET", userPurchaseKey, "qty")) or 0
    return {-1, purchased}
end

--------------------------------------------------------------------
-- Check per-user purchase limit
--------------------------------------------------------------------
local alreadyPurchased =
    tonumber(redis.call("HGET", userPurchaseKey, "qty")) or 0

if (alreadyPurchased + quantity) > maxPerUser then
    return {-2, alreadyPurchased}
end

--------------------------------------------------------------------
-- Atomic inventory update
-- Everything below executes atomically inside Redis.
--------------------------------------------------------------------
local remainingInventory =
    redis.call("DECRBY", inventoryKey, quantity)

local newUserPurchaseCount =
    redis.call(
        "HINCRBY",
        userPurchaseKey,
        "qty",
        quantity
    )

--------------------------------------------------------------------
-- Success
--------------------------------------------------------------------
return {
    remainingInventory,
    newUserPurchaseCount
}