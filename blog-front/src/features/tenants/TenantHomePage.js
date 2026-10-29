import React, { useEffect, useState } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import {
  selectAllTenants,
  selectCurrentTenant,
  tenantSelected,
  fetchTenants,
} from './tenantsSlice';
import { client } from '../../api/client';

export const TenantHomePage = () => {
  const dispatch = useDispatch();
  const tenants = useSelector(selectAllTenants);
  const currentTenant = useSelector(selectCurrentTenant);
  const [routingInfo, setRoutingInfo] = useState(null);
  const [routingError, setRoutingError] = useState(null);

  useEffect(() => {
    if (tenants.length === 0) {
      dispatch(fetchTenants());
    }
  }, [tenants.length, dispatch]);

  useEffect(() => {
    if (currentTenant) {
      setRoutingInfo(null);
      setRoutingError(null);
      client
        .get('/internal/routing-info', {
          headers: { 'X-Tenant-Id': currentTenant },
        })
        .then((data) => setRoutingInfo(data))
        .catch((err) => setRoutingError(err.message || 'Failed to fetch routing info'));
    }
  }, [currentTenant]);

  const onTenantSelect = (e) => {
    dispatch(tenantSelected(e.target.value));
  };

  const activePlacement = tenants.find((t) => t.tenantId === currentTenant);

  return (
    <section>
      <h2>Tenant & Regional Placement</h2>

      <div style={{ marginBottom: '1.5rem' }}>
        <label htmlFor="tenantsSelector" style={{ fontWeight: 'bold' }}>
          Select Active Tenant:
        </label>{' '}
        <select
          id="tenantsSelector"
          value={currentTenant}
          onChange={onTenantSelect}
          style={{ padding: '0.4rem', fontSize: '1rem', marginLeft: '0.5rem' }}
        >
          {tenants.map((t) => (
            <option key={t.tenantId} value={t.tenantId}>
              {t.tenantId} (Region: {t.region.toUpperCase()}, {t.shardId})
            </option>
          ))}
        </select>
      </div>

      {activePlacement && (
        <div style={{ padding: '1rem', border: '1px solid #ccc', borderRadius: '4px', marginBottom: '1rem' }}>
          <h3>Directory Placement Record</h3>
          <p><strong>Tenant ID:</strong> {activePlacement.tenantId}</p>
          <p><strong>Region:</strong> {activePlacement.region.toUpperCase()}</p>
          <p><strong>Assigned Shard:</strong> {activePlacement.shardId}</p>
          <p><strong>Status:</strong> <span style={{ color: activePlacement.status === 'ACTIVE' ? 'green' : 'red' }}>{activePlacement.status}</span></p>
        </div>
      )}

      <div style={{ padding: '1rem', border: '1px solid #007bff', borderRadius: '4px', backgroundColor: '#f0f7ff' }}>
        <h3>Live Routing Diagnostics (/internal/routing-info)</h3>
        {routingInfo && (
          <div>
            <p><strong>Resolved Deployment:</strong> {routingInfo.region.toUpperCase()} App Service</p>
            <p><strong>Connected Shard:</strong> {routingInfo.shardId}</p>
            <p><strong>Database Identifier:</strong> <code>{routingInfo.databaseIdentifier}</code></p>
          </div>
        )}
        {routingError && <p style={{ color: 'red' }}>{routingError}</p>}
        {!routingInfo && !routingError && <p>Resolving routing diagnostics...</p>}
      </div>
    </section>
  );
};