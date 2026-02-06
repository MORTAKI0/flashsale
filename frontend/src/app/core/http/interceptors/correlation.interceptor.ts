import { HttpInterceptorFn } from '@angular/common/http';

function generateCorrelationId(): string {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID();
  }

  return `${Date.now()}-${Math.random().toString(16).slice(2)}`;
}

export const correlationInterceptor: HttpInterceptorFn = (req, next) => {
  if (req.headers.has('X-CORRELATION-ID')) {
    return next(req);
  }

  return next(
    req.clone({
      setHeaders: {
        'X-CORRELATION-ID': generateCorrelationId(),
      },
    }),
  );
};
