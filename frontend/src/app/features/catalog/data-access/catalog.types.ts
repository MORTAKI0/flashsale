export interface ProductSummaryDto {
  productId: string;
  name: string;
  priceCents: number;
  currency: string;
}

export interface ProductDetailDto {
  productId: string;
  name: string;
  description: string | null;
  priceCents: number;
  currency: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface UpsertProductRequest {
  name: string;
  description: string | null;
  priceCents: number;
  currency: string;
}

export interface PagedResponseDto<T> {
  items: T[];
  page: number;
  size: number;
  totalItems: number;
  totalPages: number;
}
