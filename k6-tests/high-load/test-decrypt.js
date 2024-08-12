import http from 'k6/http';
import encoding from 'k6/encoding';
import { check } from 'k6';

import { FormData } from 'https://jslib.k6.io/formdata/0.0.2/index.js';
import { config } from '../config.js'
export { options } from '../options.js'

const BASE_URL = config.host;
const ENDPOINT_ENCRYPT = config.endpoint.encrypt;
const ENDPOINT_DECRYPT = config.endpoint.decrypt;
const DATA = 'data to encrypt'


export function setup() {
  const fd = new FormData()
  fd.append('encodeToB64', 'false')
  fd.append('data', { data: DATA, filename: 'data.bin', content_type: 'application/octet-stream' })
  const headers = {'Content-Type': 'multipart/form-data; boundary=' + fd.boundary}
  const res = http.post(`${BASE_URL}${ENDPOINT_ENCRYPT}`, fd.body(), { headers: headers })

  return encoding.b64encode(res.body)
}

export default function(sign) {
  const fd = new FormData()
  fd.append('cms', { data: sign, filename: 'msg.enc', content_type: 'application/octet-stream' })

  const headers = {'Content-Type': 'multipart/form-data; boundary=' + fd.boundary}
  const res = http.post(`${BASE_URL}${ENDPOINT_DECRYPT}`, fd.body(), { headers: headers });
  check(res, {
    'decrypt test status is 200': (r) => r.status === 200 ,
    'decrypt test data is equals': (r) => r.body === DATA
  });
}

export function teardown(sign) {
//  console.log(sign)
}
