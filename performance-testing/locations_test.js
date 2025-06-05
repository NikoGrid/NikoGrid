import { check } from "k6";
import http from "k6/http";

const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";

export const options = {
  // Run at 100 req/s for a 1 minute
  stages: [
    // ramp up from 0 to 200 VUs over the next 30 seconds
    { duration: "30s", target: 100 },
    // run 200 VUs over the next minute
    { duration: "1m", target: 100 },
    // ramp down from 200 to 0 VUs over the next 30 seconds
    { duration: "30s", target: 0 },
  ],
  thresholds: {
    http_req_failed: ["rate<0.01"], // http errors should be less than 1%
    http_req_duration: ["p(95)<200"], // 95% of requests should be below 200ms
  },
};

export default function () {
  http.get(
    `${BASE_URL}/api/v1/locations/nearby?w=1.8457031250000002&n=66.3375050199652&e=4.042968750000001&s=15.707662769583518&z=4`,
  );
}
