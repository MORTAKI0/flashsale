import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

interface BackendErrorPayload {
  code?: string;
  message?: string;
}

export class SafeApiError extends Error {
  constructor(
    public readonly code: string,
    public readonly status: number,
    message: string,
  ) {
    super(message);
    this.name = 'SafeApiError';
  }
}

function defaultMessageForStatus(status: number): string {
  if (status === 0) {
    return 'Network error. Please check your connection and try again.';
  }

  if (status >= 500) {
    return 'Server error. Please try again later.';
  }

  if (status === 403) {
    return 'You are not allowed to perform this action.';
  }

  if (status === 401) {
    return 'You are not authenticated. Please sign in again.';
  }

  return 'Request failed. Please try again.';
}

export const errorInterceptor: HttpInterceptorFn = (req, next) =>
  next(req).pipe(
    catchError((error: unknown) => {
      if (!(error instanceof HttpErrorResponse)) {
        return throwError(
          () => new SafeApiError('UNEXPECTED_ERROR', 0, 'Unexpected error. Please try again.'),
        );
      }

      const payload =
        typeof error.error === 'object' && error.error !== null
          ? (error.error as BackendErrorPayload)
          : undefined;

      const code = payload?.code ?? `HTTP_${error.status || 0}`;
      const message = payload?.message?.trim() || defaultMessageForStatus(error.status);

      return throwError(() => new SafeApiError(code, error.status, message));
    }),
  );
