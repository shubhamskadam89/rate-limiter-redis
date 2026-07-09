import http from "k6/http";
import { check, fail } from "k6";
import { config } from "./config.js";

/*
|--------------------------------------------------------------------------
| Authentication
|--------------------------------------------------------------------------
*/

export function login(email = config.EMAIL, password = config.PASSWORD) {

    const response = http.post(
        `${config.BASE_URL}/api/v1/auth/login`,
        JSON.stringify({
            email: email,
            password: password
        }),
        {
            headers: config.DEFAULT_HEADERS
        }
    );

    check(response, {
        "login successful": (r) => r.status === 200
    });

    if (response.status !== 200) {
        fail(`Unable to login for ${email}.\n${response.body}`);
    }

    return response.json().accessToken;
}

/*
|--------------------------------------------------------------------------
| Sale Discovery
|--------------------------------------------------------------------------
*/

export function discoverSale(jwt) {

    const response = http.get(
        `${config.BASE_URL}/api/v1/sales`,
        {
            headers: jwt ? { Authorization: `Bearer ${jwt}` } : {}
        }
    );

    check(response, {
        "sales fetched": (r) => r.status === 200
    });

    if (response.status !== 200) {
        fail("Unable to fetch sales.");
    }

    const sales = response.json();

    if (!sales.length) {
        fail("No sales available.");
    }

    const activeSales = sales.filter(s => s.status === "ACTIVE");

    if (!activeSales.length) {
        fail("No ACTIVE sale found.");
    }

    let selectedSale = null;
    let selectedItem = null;

    let highestInventory = -1;

    for (const sale of activeSales) {

        if (!sale.items || sale.items.length === 0) {
            continue;
        }

        for (const item of sale.items) {

            if (item.inventory > highestInventory) {

                highestInventory = item.inventory;

                selectedSale = sale;

                selectedItem = item;
            }
        }
    }

    if (!selectedSale || !selectedItem) {
        fail("No purchasable sale item found.");
    }

    console.log("========================================");
    console.log("Flash Sale Selected");
    console.log("----------------------------------------");
    console.log(`Sale        : ${selectedSale.name}`);
    console.log(`Status      : ${selectedSale.status}`);
    console.log(`Product     : ${selectedItem.productName}`);
    console.log(`Inventory   : ${selectedItem.inventory}`);
    console.log(`Max/User    : ${selectedItem.maxPerUser}`);
    console.log("========================================");

    return {

        saleUuid: selectedSale.saleUuid,

        saleItemUuid: selectedItem.saleItemUuid,

        productName: selectedItem.productName,

        inventory: selectedItem.inventory
    };
}

/*
|--------------------------------------------------------------------------
| UUID
|--------------------------------------------------------------------------
*/

export function uuid() {

    return "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx"
        .replace(/[xy]/g, function (c) {

            const r = Math.random() * 16 | 0;

            const v = c === "x"
                ? r
                : (r & 0x3 | 0x8);

            return v.toString(16);
        });
}

/*
|--------------------------------------------------------------------------
| Headers
|--------------------------------------------------------------------------
*/

export function authHeaders(jwt) {

    return {

        Authorization: `Bearer ${jwt}`,

        "Content-Type": "application/json",

        "X-Idempotency-Key": uuid()
    };
}

/*
|--------------------------------------------------------------------------
| Purchase
|--------------------------------------------------------------------------
*/

export function purchase(jwt, saleUuid, saleItemUuid, quantity = config.PURCHASE_QTY) {

    return http.post(

        `${config.BASE_URL}/api/v1/sales/${saleUuid}/items/${saleItemUuid}/purchase`,

        JSON.stringify({
            quantity
        }),

        {
            headers: authHeaders(jwt)
        }
    );
}