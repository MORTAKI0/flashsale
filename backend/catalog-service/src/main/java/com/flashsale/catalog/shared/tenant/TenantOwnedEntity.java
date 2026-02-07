package com.flashsale.catalog.shared.tenant;

public interface TenantOwnedEntity {

  String getTenantId();

  void setTenantId(String tenantId);
}
