import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

export interface WhoAmIResponse {
  tenantId: string;
  userId: string;
  roles: string[];
  correlationId: string;
}

@Injectable({ providedIn: 'root' })
export class CatalogApi {
  private readonly http = inject(HttpClient);

  whoAmI(): Observable<WhoAmIResponse> {
    return this.http.get<WhoAmIResponse>('/api/catalog/context/whoami');
  }
}
