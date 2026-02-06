import { Injectable } from '@angular/core';

const ACTIVE_ORG_KEY = 'activeOrgId';

@Injectable({ providedIn: 'root' })
export class ActiveOrgService {
  getActiveOrgId(): string | null {
    const value = sessionStorage.getItem(ACTIVE_ORG_KEY);
    return value?.trim() ? value : null;
  }

  setActiveOrgId(activeOrgId: string): void {
    sessionStorage.setItem(ACTIVE_ORG_KEY, activeOrgId.trim());
  }

  clearActiveOrgId(): void {
    sessionStorage.removeItem(ACTIVE_ORG_KEY);
  }
}
