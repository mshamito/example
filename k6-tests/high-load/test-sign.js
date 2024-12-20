import http from 'k6/http';
import { check } from 'k6';

import { FormData } from 'https://jslib.k6.io/formdata/0.0.2/index.js';
import { config } from '../config.js'
export { options } from '../options.js'

const BASE_URL = config.host;
const ENDPOINT = config.endpoint.sign;
const DATA = 'data to sign'
const TYPE = config.type;
const TSP = config.tsp;

export default () => {

  const fd = new FormData()
  fd.append('type', TYPE)
  fd.append('tsp', TSP)
  fd.append('detached', 'true')
  fd.append('data', { data: DATA, filename: 'data.bin', content_type: 'application/octet-stream' })

  const headers = {'Content-Type': 'multipart/form-data; boundary=' + fd.boundary}

  const res = http.post(`${BASE_URL}${ENDPOINT}`, fd.body(), { headers: headers });
  check(res, {
    'sign test status is 200': (r) => r.status === 200
  });
}
