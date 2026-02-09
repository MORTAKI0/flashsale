import { beforeEach, describe, expect, it } from 'vitest';

import { keycloak } from '../../keycloak/keycloak';
import { SessionRolesService } from './session-roles.service';

describe('SessionRolesService', () => {
  beforeEach(() => {
    (keycloak as { tokenParsed?: unknown }).tokenParsed = undefined;
  });

  it('returns empty roles when tokenParsed is undefined', () => {
    const service = new SessionRolesService();

    expect(service.getRoles()).toEqual([]);
  });

  it('returns empty roles when realm roles claim is not an array', () => {
    (keycloak as { tokenParsed?: unknown }).tokenParsed = {
      realm_access: { roles: 'OWNER' },
    };

    const service = new SessionRolesService();

    expect(service.getRoles()).toEqual([]);
  });

  it('normalizes and deduplicates realm roles', () => {
    (keycloak as { tokenParsed?: unknown }).tokenParsed = {
      realm_access: { roles: [' owner ', 'CLIENT', 'owner', 10] },
    };

    const service = new SessionRolesService();

    expect(service.getRoles()).toEqual(['OWNER', 'CLIENT']);
  });

  it('returns false for unknown or empty roles', () => {
    (keycloak as { tokenParsed?: unknown }).tokenParsed = {
      realm_access: { roles: ['CLIENT'] },
    };

    const service = new SessionRolesService();

    expect(service.hasRole('')).toBe(false);
    expect(service.hasRole('owner')).toBe(false);
    expect(service.hasRole('client')).toBe(true);
  });

  it('normalizes lookup input for hasRole', () => {
    (keycloak as { tokenParsed?: unknown }).tokenParsed = {
      realm_access: { roles: ['OWNER'] },
    };

    const service = new SessionRolesService();

    expect(service.hasRole(' owner ')).toBe(true);
    expect(service.hasRole('client')).toBe(false);
  });
});
