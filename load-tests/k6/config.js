export const config = {
    BASE_URL: __ENV.BASE_URL || "http://localhost:8080",

    EMAIL: __ENV.EMAIL || "",

    PASSWORD: __ENV.PASSWORD || "",

    SALE_UUID: __ENV.SALE_UUID || "",

    SALE_ITEM_UUID: __ENV.SALE_ITEM_UUID || "",

    PURCHASE_QTY: Number(__ENV.PURCHASE_QTY || 1),

    RATE_LIMIT_ENDPOINT: __ENV.RATE_LIMIT_ENDPOINT || "/test/limit",

    DEFAULT_HEADERS: {
        "Content-Type": "application/json"
    }
};