import { Injectable } from '@angular/core';

import { keycloak } from '../../keycloak/keycloak';

interface RealmAccessClaim {
  roles?: unknown;
}

interface TokenParsedClaim {
  realm_access?: RealmAccessClaim;
}

@Injectable({ providedIn: 'root' })
export class SessionRolesService {
  getRoles(): string[] {
    const tokenParsed = keycloak.tokenParsed as TokenParsedClaim | undefined;
    const roles = tokenParsed?.realm_access?.roles;
    if (!Array.isArray(roles)) {
      return [];
    }

    const normalizedRoles = roles
      .map((role) => this.normalizeRole(role))
      .filter((role) => role.length > 0);

    return Array.from(new Set(normalizedRoles));
  }

  hasRole(role: string): boolean {
    const normalizedRole = this.normalizeRole(role);
    if (!normalizedRole) {
      return false;
    }

    return this.getRoles().includes(normalizedRole);
  }

  private normalizeRole(role: unknown): string {
    return typeof role === 'string' ? role.trim().toUpperCase() : '';
  }
}
