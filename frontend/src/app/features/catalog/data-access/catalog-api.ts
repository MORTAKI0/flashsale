import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import {
  PagedResponseDto,
  ProductDetailDto,
  ProductSummaryDto,
  UpsertProductRequest,
} from './catalog.types';

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

  listProducts(
    page: number,
    size: number,
    q?: string,
  ): Observable<PagedResponseDto<ProductSummaryDto>> {
    let params = new HttpParams().set('page', String(page)).set('size', String(size));

    const trimmedQ = q?.trim();
    if (trimmedQ) {
      params = params.set('q', trimmedQ);
    }

    return this.http.get<PagedResponseDto<ProductSummaryDto>>('/api/catalog/products', {
      params,
    });
  }

  getProduct(productId: string): Observable<ProductDetailDto> {
    return this.http.get<ProductDetailDto>(
      `/api/catalog/products/${encodeURIComponent(productId)}`,
    );
  }

  createProduct(request: UpsertProductRequest): Observable<ProductDetailDto> {
    return this.http.post<ProductDetailDto>('/api/catalog/products', request);
  }

  updateProduct(productId: string, request: UpsertProductRequest): Observable<ProductDetailDto> {
    return this.http.put<ProductDetailDto>(
      `/api/catalog/products/${encodeURIComponent(productId)}`,
      request,
    );
  }

  deleteProduct(productId: string): Observable<void> {
    return this.http.delete<void>(`/api/catalog/products/${encodeURIComponent(productId)}`);
  }
}
