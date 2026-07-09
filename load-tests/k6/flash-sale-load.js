import { check } from "k6";
import { Counter, Rate, Trend } from "k6/metrics";
import { htmlReport } from "./bundle.js";

import { login, purchase, discoverSale } from "./helpers.js";

/*
|--------------------------------------------------------------------------
| Custom Metrics
|--------------------------------------------------------------------------
*/

export const successCounter = new Counter("purchase_success");

export const soldOutCounter = new Counter("purchase_sold_out");

export const rateLimitedCounter = new Counter("purchase_rate_limited");

export const unexpectedCounter = new Counter("purchase_unexpected");

export const successRate = new Rate("purchase_success_rate");

export const purchaseLatency = new Trend("purchase_latency");

/*
|--------------------------------------------------------------------------
| Test Configuration
|--------------------------------------------------------------------------
*/

export const options = {

    scenarios: {

        flash_sale: {

            executor: "constant-vus",

            vus: Number(__ENV.VUS || 500),

            duration: __ENV.DURATION || "30s"
        }
    },

    thresholds: {

        http_req_failed: ["rate<0.01"],

        http_req_duration: ["p(95)<200"],

        purchase_success_rate: ["rate>0.90"]
    }
};

/*
|--------------------------------------------------------------------------
| Login Once
|--------------------------------------------------------------------------
*/

export function setup() {

    console.log("Authenticating setup user...");

    const token = login("user_1@flashsale.com", "password123");

    console.log("Authentication successful.");

    const sale = discoverSale(token);

    return {

        saleUuid: sale.saleUuid,

        saleItemUuid: sale.saleItemUuid,

        productName: sale.productName,

        inventory: sale.inventory
    };
}

/*
|--------------------------------------------------------------------------
| Main Load Test
|--------------------------------------------------------------------------
*/

let vuToken = null;

export default function (data) {

    if (!vuToken) {
        const vuId = __VU > 0 ? __VU : 1;
        vuToken = login(`user_${vuId}@flashsale.com`, "password123");
    }

    const response = purchase(
        vuToken,
        data.saleUuid,
        data.saleItemUuid
    );

    purchaseLatency.add(response.timings.duration);

    switch (response.status) {

        case 200:

            successCounter.add(1);

            successRate.add(true);

            check(response, {

                "purchase successful": (r) =>
                    r.json("remainingInventory") >= 0

            });

            break;

        case 409:

            soldOutCounter.add(1);

            successRate.add(false);

            break;

        case 429:

            rateLimitedCounter.add(1);

            successRate.add(false);

            break;

        default:

            unexpectedCounter.add(1);

            successRate.add(false);

            console.error(
                `Unexpected response ${response.status}: ${response.body}`
            );
    }
}

/*
|--------------------------------------------------------------------------
| End-of-Test Summary
|--------------------------------------------------------------------------
*/

export function handleSummary(data) {

    const avg = data.metrics.http_req_duration?.values?.avg ?? 0;
    const p95 = data.metrics.http_req_duration?.values?.["p(95)"] ?? 0;

    const vus = __ENV.VUS || 500;
    const duration = __ENV.DURATION || "30s";

    const textSummary = `
============================================================

Flash Sale Load Test Summary

Virtual Users : ${vus}

Duration      : ${duration}

Requests      : ${data.metrics.http_reqs?.values?.count ?? 0}

Avg Latency   : ${avg.toFixed(2)} ms

P95 Latency   : ${p95.toFixed(2)} ms

Success       : ${data.metrics.purchase_success?.values?.count ?? 0}

Sold Out      : ${data.metrics.purchase_sold_out?.values?.count ?? 0}

Rate Limited  : ${data.metrics.purchase_rate_limited?.values?.count ?? 0}

Unexpected    : ${data.metrics.purchase_unexpected?.values?.count ?? 0}

============================================================
`;

    const htmlPath = __ENV.HTML_REPORT_PATH || "report.html";

    return {
        "stdout": textSummary,
        [htmlPath]: htmlReport(data)
    };
}