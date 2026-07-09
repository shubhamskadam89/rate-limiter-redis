import http from "k6/http";
import { check } from "k6";
import { Counter, Rate, Trend } from "k6/metrics";
import { htmlReport } from "./bundle.js";

import { login, discoverSale, authHeaders } from "./helpers.js";
import { config } from "./config.js";

/*
|--------------------------------------------------------------------------
| Metrics
|--------------------------------------------------------------------------
*/

const allowedRequests = new Counter("rate_limit_allowed");

const blockedRequests = new Counter("rate_limit_blocked");

const unexpectedRequests = new Counter("rate_limit_unexpected");

const successRate = new Rate("rate_limit_success");

const latency = new Trend("rate_limit_latency");

/*
|--------------------------------------------------------------------------
| Configuration
|--------------------------------------------------------------------------
*/

export const options = {

    scenarios: {

        rate_limit: {

            executor: "constant-vus",

            vus: Number(__ENV.VUS || 50),

            duration: __ENV.DURATION || "30s"
        }
    },

    thresholds: {

        http_req_failed: ["rate<0.01"],

        http_req_duration: ["p(95)<100"],

        rate_limit_success: ["rate>0.05"],

        rate_limit_blocked: ["count>0"]
    }
};

/*
|--------------------------------------------------------------------------
| Setup
|--------------------------------------------------------------------------
*/

export function setup() {

    console.log("Authenticating...");

    const token = login();

    console.log("Discovering active sale...");

    let sale = null;

    try {

        sale = discoverSale(token);

    } catch (e) {

        console.log("No active sale found for auto-discovery: " + e.message);

    }

    return {

        token,

        saleUuid: sale?.saleUuid ?? "",

        saleItemUuid: sale?.saleItemUuid ?? ""

    };
}

/*
|--------------------------------------------------------------------------
| Main Test
|--------------------------------------------------------------------------
*/

export default function (data) {

    let endpoint = config.RATE_LIMIT_ENDPOINT;

    if (data.saleUuid && data.saleItemUuid) {

        endpoint = endpoint

            .replace("{saleUuid}", data.saleUuid)

            .replace("{saleItemUuid}", data.saleItemUuid);

    }

    let response;

    if (endpoint.endsWith("/purchase")) {

        response = http.post(

            `${config.BASE_URL}${endpoint}`,

            JSON.stringify({
                quantity: 1
            }),

            {
                headers: authHeaders(data.token)
            }

        );

    } else {

        response = http.get(

            `${config.BASE_URL}${endpoint}`,

            {
                headers: {

                    Authorization: `Bearer ${data.token}`
                }
            }

        );

    }

    latency.add(response.timings.duration);

    switch (response.status) {

        case 200:
        case 400:
        case 403:
        case 409:

            allowedRequests.add(1);

            successRate.add(true);

            break;

        case 429:

            blockedRequests.add(1);

            successRate.add(false);

            break;

        default:

            unexpectedRequests.add(1);

            successRate.add(false);

            console.warn(
                `${response.status} -> ${response.body}`
            );
    }

    check(response, {

        "response is valid": (r) =>
            r.status === 200 || r.status === 400 || r.status === 403 || r.status === 409 || r.status === 429
    });
}

/*
|--------------------------------------------------------------------------
| Summary
|--------------------------------------------------------------------------
*/

export function handleSummary(data) {

    const avg = data.metrics.http_req_duration?.values?.avg ?? 0;
    const p95 = data.metrics.http_req_duration?.values?.["p(95)"] ?? 0;

    const algo = __ENV.ALGO || "UNKNOWN";
    const endpoint = config.RATE_LIMIT_ENDPOINT;
    const vus = __ENV.VUS || "50";
    const duration = __ENV.DURATION || "30s";
    const date = __ENV.DATE || new Date().toISOString().split('T')[0];
    const commit = __ENV.GIT_COMMIT || "N/A";

    let resolvedEndpoint = endpoint;
    if (data && data.saleUuid && data.saleItemUuid) {
        resolvedEndpoint = resolvedEndpoint
            .replace("{saleUuid}", data.saleUuid)
            .replace("{saleItemUuid}", data.saleItemUuid);
    }

    const textSummary = `
======================================================
Benchmark Information
======================================================
Algorithm          : ${algo}
Endpoint           : ${resolvedEndpoint}
VUs                : ${vus}
Duration           : ${duration}
Date               : ${date}
Git Commit         : ${commit}
======================================================

============================================================

Rate Limiter Benchmark

Allowed Requests : ${data.metrics.rate_limit_allowed?.values?.count ?? 0}

Blocked Requests : ${data.metrics.rate_limit_blocked?.values?.count ?? 0}

Unexpected       : ${data.metrics.rate_limit_unexpected?.values?.count ?? 0}

Average Latency  : ${avg.toFixed(2)} ms

P95 Latency      : ${p95.toFixed(2)} ms

============================================================

`;

    const htmlPath = __ENV.HTML_REPORT_PATH || "report.html";

    return {
        "stdout": textSummary,
        [htmlPath]: htmlReport(data)
    };
}