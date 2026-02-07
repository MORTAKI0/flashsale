import { CommonModule } from '@angular/common';
import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';

import { SafeApiError } from '../../../../core/http/interceptors/error.interceptor';
import { CatalogApi } from '../../data-access/catalog-api';
import { ProductSummaryDto } from '../../data-access/catalog.types';

@Component({
  selector: 'app-products-list-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './products-list.page.html',
  styleUrl: './products-list.page.scss',
})
export class ProductsListPage implements OnInit {
  private readonly catalogApi = inject(CatalogApi);
  private readonly destroyRef = inject(DestroyRef);

  items: ProductSummaryDto[] = [];
  q = '';

  page = 0;
  size = 20;
  totalItems = 0;
  totalPages = 0;

  loading = false;
  error = '';

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    this.loading = true;
    this.error = '';

    this.catalogApi
      .listProducts(this.page, this.size, this.q)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (response) => {
          this.items = response.items;
          this.page = response.page;
          this.size = response.size;
          this.totalItems = response.totalItems;
          this.totalPages = response.totalPages;
          this.loading = false;
        },
        error: (err: unknown) => {
          this.items = [];
          this.loading = false;

          if (err instanceof SafeApiError) {
            this.error = `${err.code}: ${err.message}`;
            return;
          }

          this.error = 'UNEXPECTED_ERROR: Unexpected error. Please try again.';
        },
      });
  }

  onSearch(): void {
    this.page = 0;
    this.loadProducts();
  }

  onPageSizeChange(sizeValue: string): void {
    const parsedSize = Number(sizeValue);
    if (!Number.isFinite(parsedSize) || parsedSize <= 0) {
      return;
    }

    this.size = parsedSize;
    this.page = 0;
    this.loadProducts();
  }

  prevPage(): void {
    if (!this.hasPrevPage || this.loading) {
      return;
    }

    this.page -= 1;
    this.loadProducts();
  }

  nextPage(): void {
    if (!this.hasNextPage || this.loading) {
      return;
    }

    this.page += 1;
    this.loadProducts();
  }

  formatPrice(priceCents: number): string {
    return (priceCents / 100).toFixed(2);
  }

  get uiPage(): number {
    return this.page + 1;
  }

  get hasPrevPage(): boolean {
    return this.page > 0;
  }

  get hasNextPage(): boolean {
    return this.page + 1 < this.totalPages;
  }
}
