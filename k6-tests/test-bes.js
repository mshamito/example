import http from 'k6/http';

import { check, group, sleep, fail } from 'k6';
import encoding from 'k6/encoding';
import { FormData } from 'https://jslib.k6.io/formdata/0.0.2/index.js';

export const options = {

  vus: 10,

  duration: '10s',

  insecureSkipTLSVerify: true,

  thresholds: {

    http_req_duration: ['p(99)<1500'], // 99% of requests must complete below 1.5s

  },

};

const BASE_URL = 'http://localhost:8080';

const ENDPOINT = '/sign';

export default () => {
  const fd = new FormData();

  fd.append('type', 'bes');
  fd.append('tsp', '');

  fd.append('detached', 'true');

  fd.append('data', { data: 'data to sign', filename: 'data.bin', content_type: 'application/octet-stream' });

  const headers = {'Content-Type': 'multipart/form-data; boundary=' + fd.boundary}

  const res = http.post(`${BASE_URL}${ENDPOINT}`, fd.body(), { headers: headers });
  check(res, {
    'sign test status is 200': (r) => r.status === 200
  });
}
