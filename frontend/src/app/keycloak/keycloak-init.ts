// src/app/keycloak/keycloak-init.ts
import { keycloak } from './keycloak';

export function initializeKeycloak() {
  return () =>
    keycloak.init({
      onLoad: 'check-sso',            // don't force login on app start
      pkceMethod: 'S256',             // PKCE
      checkLoginIframe: false,        // simpler for local dev
      redirectUri: window.location.origin,
    });
}
