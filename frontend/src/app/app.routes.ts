import { Routes } from '@angular/router';

import { ProductsListPage } from './features/catalog/pages/products-list/products-list.page';

export const routes: Routes = [
  { path: 'catalog/products', component: ProductsListPage },
  { path: '', pathMatch: 'full', redirectTo: 'catalog/products' },
];
