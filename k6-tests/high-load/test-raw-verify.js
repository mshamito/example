import http from 'k6/http';
import encoding from 'k6/encoding';
import { check } from 'k6';

import { FormData } from 'https://jslib.k6.io/formdata/0.0.2/index.js';
import { config } from '../config.js'
export { options } from '../options.js'

const BASE_URL = config.host;
const ENDPOINT_SIGN = config.endpoint.rawSign;
const ENDPOINT_VERIFY = config.endpoint.rawVerify;
const DATA = 'data to sign'


export function setup() {
  const fd = new FormData()
  fd.append('data', { data: DATA, filename: 'data.bin', content_type: 'application/octet-stream' })
  fd.append('encodeToB64', 'false')
  fd.append('invert', 'false')
  const headers = {'Content-Type': 'multipart/form-data; boundary=' + fd.boundary}
  const res = http.post(`${BASE_URL}${ENDPOINT_SIGN}`, fd.body(), { headers: headers })

  return encoding.b64encode(res.body)
}

export default function(sign) {
  const fd = new FormData()
  fd.append('data', { data: DATA, filename: 'data.bin', content_type: 'application/octet-stream' })
  fd.append('signBase64', sign)

  const headers = {'Content-Type': 'multipart/form-data; boundary=' + fd.boundary}
  const res = http.post(`${BASE_URL}${ENDPOINT_VERIFY}`, fd.body(), { headers: headers });
  check(res, {
    'raw verify test status is 200': (r) => r.status === 200,
    'verify result is true': (r) => r.body === 'true'
  });
}

export function teardown(sign) {
//  console.log(sign)
}
