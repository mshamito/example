import { config } from './config.js';

export const options = {
  vus: config.vus,
  duration: config.duration,
  insecureSkipTLSVerify: true,
  thresholds: {
    http_req_duration: ['p(99)<3000'], // 99% of requests must complete below 3s
  },
}
