import { CommonModule } from '@angular/common';
import { Component, DestroyRef, ElementRef, OnInit, ViewChild, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';

import { SessionRolesService } from '../../../../core/auth/session-roles.service';
import { SafeApiError } from '../../../../core/http/interceptors/error.interceptor';
import { CatalogApi } from '../../data-access/catalog-api';
import {
  ProductDetailDto,
  ProductSummaryDto,
  UpsertProductRequest,
} from '../../data-access/catalog.types';

type FormMode = 'create' | 'edit';
type ToastKind = 'success' | 'error' | 'info';

interface ProductFormModel {
  name: string;
  description: string;
  priceCentsInput: string;
  currency: string;
}

interface ProductFormErrors {
  name: string;
  priceCents: string;
  currency: string;
}

@Component({
  selector: 'app-products-list-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './products-list.page.html',
  styleUrls: ['./products-list.page.scss'],
})
export class ProductsListPage implements OnInit {
  private readonly catalogApi = inject(CatalogApi);
  private readonly sessionRolesService = inject(SessionRolesService);
  private readonly destroyRef = inject(DestroyRef);
  private toastTimer: ReturnType<typeof setTimeout> | null = null;

  @ViewChild('editNameInput') editNameInput?: ElementRef<HTMLInputElement>;

  items: ProductSummaryDto[] = [];
  q = '';

  page = 0;
  size = 20;
  totalItems = 0;
  totalPages = 0;

  loading = false;
  createSubmitting = false;
  loadingEditProductId: string | null = null;
  updateSubmitting = false;
  deletingProductId: string | null = null;
  confirmingDeleteProductId: string | null = null;
  error = '';
  toastMessage = '';
  toastKind: ToastKind = 'success';

  createForm: ProductFormModel = this.emptyForm();
  createErrors: ProductFormErrors = this.emptyErrors();
  createApiError = '';
  editingProductId: string | null = null;
  editForm: ProductFormModel = this.emptyForm();
  editErrors: ProductFormErrors = this.emptyErrors();
  editApiError = '';

  ngOnInit(): void {
    this.destroyRef.onDestroy(() => {
      if (this.toastTimer !== null) {
        clearTimeout(this.toastTimer);
      }
    });

    this.loadProducts();
  }

  loadProducts(): void {
    this.confirmingDeleteProductId = null;
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
          this.error = this.getListErrorMessage(err);
        },
      });
  }

  createProduct(): void {
    if (!this.canManageProducts || this.createSubmitting) {
      return;
    }

    this.createApiError = '';
    const payload = this.normalizedPayload(this.createForm, 'create');
    if (!payload) {
      return;
    }

    this.createSubmitting = true;
    this.catalogApi
      .createProduct(payload)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.createSubmitting = false;
          this.createForm = this.emptyForm();
          this.createErrors = this.emptyErrors();
          this.page = 0;
          this.showToast('Product created.', 'success');
          this.loadProducts();
        },
        error: (err: unknown) => {
          this.createSubmitting = false;
          this.createApiError = this.getActionErrorMessage(err);
          this.showToast(this.createApiError, 'error');
        },
      });
  }

  startEdit(productId: string): void {
    if (!this.canManageProducts || this.loadingEditProductId !== null || this.updateSubmitting) {
      return;
    }

    this.editApiError = '';
    this.loadingEditProductId = productId;
    this.catalogApi
      .getProduct(productId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (product) => {
          this.editingProductId = productId;
          this.editForm = this.mapDetailToForm(product);
          this.editErrors = this.emptyErrors();
          this.loadingEditProductId = null;
          this.confirmingDeleteProductId = null;
          this.focusEditForm();
        },
        error: (err: unknown) => {
          this.loadingEditProductId = null;
          this.editApiError = this.getActionErrorMessage(err);
          this.showToast(this.editApiError, 'error');
          if (this.isNotFound(err)) {
            this.loadProducts();
          }
        },
      });
  }

  cancelEdit(): void {
    this.editingProductId = null;
    this.editForm = this.emptyForm();
    this.editErrors = this.emptyErrors();
    this.editApiError = '';
  }

  updateProduct(): void {
    if (!this.canManageProducts || !this.editingProductId || this.updateSubmitting) {
      return;
    }

    this.editApiError = '';
    const payload = this.normalizedPayload(this.editForm, 'edit');
    if (!payload) {
      return;
    }

    this.updateSubmitting = true;
    this.catalogApi
      .updateProduct(this.editingProductId, payload)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.updateSubmitting = false;
          this.editingProductId = null;
          this.editForm = this.emptyForm();
          this.editErrors = this.emptyErrors();
          this.showToast('Product updated.', 'success');
          this.loadProducts();
        },
        error: (err: unknown) => {
          this.updateSubmitting = false;
          this.editApiError = this.getActionErrorMessage(err);
          this.showToast(this.editApiError, 'error');
          if (this.isNotFound(err)) {
            this.cancelEdit();
            this.loadProducts();
          }
        },
      });
  }

  requestDelete(productId: string): void {
    if (!this.canManageProducts || this.deletingProductId !== null) {
      return;
    }

    this.confirmingDeleteProductId = productId;
  }

  cancelDelete(): void {
    this.confirmingDeleteProductId = null;
  }

  confirmDelete(productId: string, productName: string): void {
    if (!this.canManageProducts || this.deletingProductId !== null) {
      return;
    }

    this.deletingProductId = productId;
    this.confirmingDeleteProductId = null;
    this.catalogApi
      .deleteProduct(productId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.deletingProductId = null;
          this.showToast(`Product "${productName}" deleted.`, 'success');
          if (this.items.length === 1 && this.page > 0) {
            this.page -= 1;
          }
          this.loadProducts();
        },
        error: (err: unknown) => {
          this.deletingProductId = null;
          this.showToast(this.getActionErrorMessage(err), 'error');
          if (this.isNotFound(err)) {
            this.loadProducts();
          }
        },
      });
  }

  onSearch(): void {
    this.confirmingDeleteProductId = null;
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
    this.confirmingDeleteProductId = null;
    this.loadProducts();
  }

  prevPage(): void {
    if (!this.hasPrevPage || this.loading) {
      return;
    }

    this.page -= 1;
    this.confirmingDeleteProductId = null;
    this.loadProducts();
  }

  nextPage(): void {
    if (!this.hasNextPage || this.loading) {
      return;
    }

    this.page += 1;
    this.confirmingDeleteProductId = null;
    this.loadProducts();
  }

  formatPrice(priceCents: number): string {
    return (priceCents / 100).toFixed(2);
  }

  trackByProductId(_: number, item: ProductSummaryDto): string {
    return item.productId;
  }

  normalizeName(mode: FormMode): void {
    this.formFor(mode).name = this.formFor(mode).name.trim();
  }

  normalizeDescription(mode: FormMode): void {
    this.formFor(mode).description = this.formFor(mode).description.trim();
  }

  normalizeCurrency(mode: FormMode): void {
    this.formFor(mode).currency = this.formFor(mode).currency.trim().toUpperCase();
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

  get canManageProducts(): boolean {
    return this.sessionRolesService.hasRole('OWNER');
  }

  private emptyForm(): ProductFormModel {
    return {
      name: '',
      description: '',
      priceCentsInput: '0',
      currency: 'USD',
    };
  }

  private emptyErrors(): ProductFormErrors {
    return {
      name: '',
      priceCents: '',
      currency: '',
    };
  }

  private formFor(mode: FormMode): ProductFormModel {
    return mode === 'create' ? this.createForm : this.editForm;
  }

  private setFormErrors(mode: FormMode, errors: ProductFormErrors): void {
    if (mode === 'create') {
      this.createErrors = errors;
      return;
    }

    this.editErrors = errors;
  }

  private normalizedPayload(form: ProductFormModel, mode: FormMode): UpsertProductRequest | null {
    const name = form.name.trim();
    const currency = form.currency.trim().toUpperCase();
    const description = form.description?.trim() || null;
    const priceCentsText = form.priceCentsInput.trim();
    const errors = this.emptyErrors();

    if (name.length < 2 || name.length > 200) {
      errors.name = 'Name must be between 2 and 200 characters.';
    }

    if (!/^[A-Z]{3}$/.test(currency)) {
      errors.currency = 'Currency must be 3 uppercase letters (e.g. USD).';
    }

    if (!/^\d+$/.test(priceCentsText)) {
      errors.priceCents = 'Price must be a non-negative whole number of cents.';
    }

    if (errors.name || errors.currency || errors.priceCents) {
      this.setFormErrors(mode, errors);
      return null;
    }

    const parsedPriceCents = Number(priceCentsText);
    if (!Number.isSafeInteger(parsedPriceCents)) {
      errors.priceCents = 'Price is too large.';
      this.setFormErrors(mode, errors);
      return null;
    }

    this.setFormErrors(mode, this.emptyErrors());
    return {
      name,
      description,
      priceCents: parsedPriceCents,
      currency,
    };
  }

  private getActionErrorMessage(err: unknown): string {
    if (err instanceof SafeApiError) {
      if (err.status === 404) {
        return 'Product not found. It may have been deleted already.';
      }

      if (err.status === 403) {
        return 'Not allowed.';
      }

      if (err.status === 400) {
        return `${err.code}: ${err.message}`;
      }

      return 'UNEXPECTED_ERROR: Unexpected error. Please try again.';
    }

    return 'UNEXPECTED_ERROR: Unexpected error. Please try again.';
  }

  private getListErrorMessage(err: unknown): string {
    return this.getActionErrorMessage(err);
  }

  private isNotFound(err: unknown): boolean {
    return err instanceof SafeApiError && err.status === 404;
  }

  private mapDetailToForm(product: ProductDetailDto): ProductFormModel {
    return {
      name: product.name,
      description: product.description ?? '',
      priceCentsInput: String(product.priceCents),
      currency: product.currency.toUpperCase(),
    };
  }

  private focusEditForm(): void {
    setTimeout(() => {
      const input = this.editNameInput?.nativeElement;
      if (!input) {
        return;
      }

      input.focus();
      input.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }, 0);
  }

  private showToast(message: string, kind: ToastKind): void {
    if (this.toastTimer !== null) {
      clearTimeout(this.toastTimer);
      this.toastTimer = null;
    }

    this.toastMessage = message;
    this.toastKind = kind;
    this.toastTimer = setTimeout(() => {
      this.toastMessage = '';
      this.toastTimer = null;
    }, 4500);
  }
}
