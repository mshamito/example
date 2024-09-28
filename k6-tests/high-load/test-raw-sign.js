import http from 'k6/http';
import { check } from 'k6';

import { FormData } from 'https://jslib.k6.io/formdata/0.0.2/index.js';
import { config } from '../config.js'
export { options } from '../options.js'

const BASE_URL = config.host;
const ENDPOINT = config.endpoint.rawSign;
const DATA = 'data to sign'
const TYPE = config.type;
const TSP = config.tsp;

export default () => {

  const fd = new FormData()
  fd.append('data', { data: DATA, filename: 'data.bin', content_type: 'application/octet-stream' })
  fd.append('encodeToB64', false)
  fd.append('invert', false)

  const headers = {'Content-Type': 'multipart/form-data; boundary=' + fd.boundary}

  const res = http.post(`${BASE_URL}${ENDPOINT}`, fd.body(), { headers: headers });
  check(res, {
    'raw sign test status is 200': (r) => r.status === 200
  });
}
