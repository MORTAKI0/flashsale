import { CommonModule } from '@angular/common';
import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { keycloak } from './keycloak/keycloak';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet],
  templateUrl: './app.html',
  styleUrls: ['./app.scss'],
})
export class App {
  protected readonly title = signal('frontend');
  tokenPreview = '';

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
}
