export const config = {
  vus: 10,
  duration: '10s',
  host: 'http://localhost:8080',
  tsp: 'http://testca2012.cryptopro.ru/tsp/tsp.srf',
  endpoint: {
    sign: '/sign',
    verify: '/verify',
    encrypt: '/encr',
    decrypt: '/decr'
  }
}
