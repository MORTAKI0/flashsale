import { CommonModule } from '@angular/common';
import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterOutlet } from '@angular/router';
import { firstValueFrom } from 'rxjs';

import { SafeApiError } from './core/http/interceptors/error.interceptor';
import { ActiveOrgService } from './core/tenant/active-org.service';
import { CatalogApi, WhoAmIResponse } from './features/catalog/data-access/catalog-api';
import { keycloak } from './keycloak/keycloak';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterOutlet],
  templateUrl: './app.html',
  styleUrls: ['./app.scss'],
})
export class App {
  protected readonly title = signal('frontend');
  tokenPreview = '';

  activeOrgInput = '';
  savedActiveOrgId: string | null = null;

  loadingWhoAmI = false;
  whoAmIResponse: WhoAmIResponse | null = null;
  apiError = '';

  constructor(
    private readonly activeOrgService: ActiveOrgService,
    private readonly catalogApi: CatalogApi,
  ) {
    const currentOrg = this.activeOrgService.getActiveOrgId();
    this.savedActiveOrgId = currentOrg;
    this.activeOrgInput = currentOrg ?? '';
  }

  get authenticated() {
    return keycloak.authenticated;
  }

  get username() {
    return keycloak.tokenParsed?.['preferred_username'];
  }

  login() {
    keycloak.login({ redirectUri: window.location.href });
  }

  logout() {
    keycloak.logout({ redirectUri: window.location.origin });
  }

  printToken(): void {
    console.log('--- KEYCLOAK TOKENS ---');
    console.log('Access token:', keycloak.token);
    console.log('Refresh token:', keycloak.refreshToken);
    console.log('ID token:', keycloak.idToken);
    console.log('Token parsed:', keycloak.tokenParsed);
    console.log('Authenticated:', keycloak.authenticated);

    if (!keycloak.token) {
      alert('No access token found. Check console + confirm Keycloak init/login succeeded.');
    }
  }

  showToken(): void {
    this.tokenPreview = keycloak.token ?? '(no token)';
  }

  saveActiveOrgId(): void {
    const trimmed = (this.activeOrgInput ?? '').trim();

    if (!trimmed) {
      this.activeOrgService.clearActiveOrgId();
      this.savedActiveOrgId = null;
      this.activeOrgInput = '';
      return;
    }

    this.activeOrgService.setActiveOrgId(trimmed);
    const storedValue = this.activeOrgService.getActiveOrgId();
    this.savedActiveOrgId = storedValue;
    this.activeOrgInput = this.savedActiveOrgId ?? '';
  }

  clearActiveOrgId(): void {
    this.activeOrgService.clearActiveOrgId();
    this.savedActiveOrgId = null;
    this.activeOrgInput = '';
  }

  async callWhoAmI(): Promise<void> {
    this.loadingWhoAmI = true;
    this.apiError = '';
    this.whoAmIResponse = null;

    try {
      const activeOrgId = this.activeOrgService.getActiveOrgId();
      if (!activeOrgId) {
        this.apiError = 'Missing activeOrgId. Set it and click Save activeOrgId before calling whoAmI.';
        return;
      }

      await keycloak.updateToken(30);
      if (!keycloak.token) {
        this.apiError = 'Missing access token. Please log in again.';
        return;
      }

      this.whoAmIResponse = await firstValueFrom(this.catalogApi.whoAmI());
    } catch (error) {
      if (error instanceof SafeApiError) {
        this.apiError = `${error.code}: ${error.message}`;
      } else {
        this.apiError = 'Unexpected error. Please try again.';
      }
    } finally {
      this.loadingWhoAmI = false;
    }
  }
}
