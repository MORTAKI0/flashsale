import { inject } from '@angular/core';
import { HttpInterceptorFn } from '@angular/common/http';

import { keycloak } from '../../../keycloak/keycloak';
import { ActiveOrgService } from '../../tenant/active-org.service';
import { isApiRequest, isPublicRequest } from './request-url.util';

const DEBUG_ORG_INTERCEPTOR = false;

export const orgInterceptor: HttpInterceptorFn = (req, next) => {
  const isApi = isApiRequest(req.url);
  const isPublic = isPublicRequest(req.url);
  const tokenLength = keycloak.token?.length ?? 0;
  if (!isApi || isPublic) {
    if (DEBUG_ORG_INTERCEPTOR) {
      console.log('[orgInterceptor]', {
        url: req.url,
        isApi,
        isPublic,
        activeOrgId: null,
        tokenLength,
        orgHeaderAdded: false,
      });
    }
    return next(req);
  }

  const activeOrgService = inject(ActiveOrgService);
  const activeOrgId = activeOrgService.getActiveOrgId();
  if (DEBUG_ORG_INTERCEPTOR) {
    console.log('[orgInterceptor]', {
      url: req.url,
      isApi,
      isPublic,
      activeOrgId,
      tokenLength,
      orgHeaderAdded: Boolean(activeOrgId),
    });
  }

  if (!activeOrgId) {
    return next(req);
  }

  return next(
    req.clone({
      setHeaders: {
        'X-ORG-ID': activeOrgId,
      },
    }),
  );
};
