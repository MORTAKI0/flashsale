// src/app/keycloak/keycloak.ts
import Keycloak from 'keycloak-js';

export const keycloak = new Keycloak({
  url: 'http://localhost:9090',
  realm: 'flashsale',
  clientId: 'flashsale-spa',
});
