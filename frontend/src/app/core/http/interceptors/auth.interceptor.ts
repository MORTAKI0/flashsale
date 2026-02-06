import { HttpInterceptorFn } from '@angular/common/http';

import { keycloak } from '../../../keycloak/keycloak';
import { isApiRequest, isPublicRequest } from './request-url.util';

const DEBUG_AUTH_INTERCEPTOR = false;

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const isApi = isApiRequest(req.url);
  const isPublic = isPublicRequest(req.url);
  const activeOrgId = sessionStorage.getItem('activeOrgId');
  if (!isApi) {
    if (DEBUG_AUTH_INTERCEPTOR) {
      console.log('[authInterceptor]', {
        url: req.url,
        isApi,
        isPublic,
        tokenLength: 0,
        activeOrgId,
        authorizationAdded: false,
      });
    }
    return next(req);
  }

  const token = keycloak.token;
  if (!token) {
    if (DEBUG_AUTH_INTERCEPTOR) {
      console.log('[authInterceptor]', {
        url: req.url,
        isApi,
        isPublic,
        tokenLength: 0,
        activeOrgId,
        authorizationAdded: false,
      });
    }
    return next(req);
  }

  const authorizationAdded = true;
  if (DEBUG_AUTH_INTERCEPTOR) {
    console.log('[authInterceptor]', {
      url: req.url,
      isApi,
      isPublic,
      tokenLength: token.length,
      activeOrgId,
      authorizationAdded,
    });
  }

  return next(
    req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`,
      },
    }),
  );
};
