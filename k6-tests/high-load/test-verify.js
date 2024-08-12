import http from 'k6/http';
import encoding from 'k6/encoding';
import { check } from 'k6';

import { FormData } from 'https://jslib.k6.io/formdata/0.0.2/index.js';
import { config } from '../config.js'
export { options } from '../options.js'

const BASE_URL = config.host;
const ENDPOINT_SIGN = config.endpoint.sign;
const ENDPOINT_VERIFY = config.endpoint.verify;
const DATA = 'data to sign'


export function setup() {
  const fd = new FormData()
  fd.append('type', 'bes')
  fd.append('tsp', 'http://testca2012.cryptopro.ru/tsp/tsp.srf')
  fd.append('detached', 'true')
  fd.append('encodeToB64', 'false')
  fd.append('data', { data: DATA, filename: 'data.bin', content_type: 'application/octet-stream' })
  const headers = {'Content-Type': 'multipart/form-data; boundary=' + fd.boundary}
  const res = http.post(`${BASE_URL}${ENDPOINT_SIGN}`, fd.body(), { headers: headers })

  return encoding.b64encode(res.body)
}

export default function(sign) {
  const fd = new FormData()
  fd.append('data', { data: DATA, filename: 'data.bin', content_type: 'application/octet-stream' })
  fd.append('sign', { data: sign, filename: 'sign.sig', content_type: 'application/octet-stream' })

  const headers = {'Content-Type': 'multipart/form-data; boundary=' + fd.boundary}
  const res = http.post(`${BASE_URL}${ENDPOINT_VERIFY}`, fd.body(), { headers: headers });
  check(res, {
    'verify test status is 200': (r) => r.status === 200
  });
}

export function teardown(sign) {
//  console.log(sign)
}
