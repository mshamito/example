import { config } from '../config.js';

import http from 'k6/http';
import { check } from 'k6';
import { FormData } from 'https://jslib.k6.io/formdata/0.0.2/index.js';

export const options = {
  vus: config.vus,
  duration: config.duration,
  insecureSkipTLSVerify: true,
  thresholds: {
    http_req_duration: ['p(99)<3000'], // 99% of requests must complete below 3s
  },
};

const BASE_URL = config.host;
const ENDPOINT = config.endpoint.sign;

export default () => {
  const fd = new FormData();

  fd.append('type', 't');
  fd.append('tsp', config.tsp);
  fd.append('detached', 'true');
  fd.append('data', { data: 'data to sign', filename: 'data.bin', content_type: 'application/octet-stream' });

  const headers = {'Content-Type': 'multipart/form-data; boundary=' + fd.boundary}

  const res = http.post(`${BASE_URL}${ENDPOINT}`, fd.body(), { headers: headers });
  check(res, {
    'sign test status is 200': (r) => r.status === 200
  });
}
