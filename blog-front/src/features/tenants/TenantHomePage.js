import React, { useEffect, useState } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import {
  selectAllTenants,
  selectCurrentTenant,
  tenantSelected,
  fetchTenants,
} from './tenantsSlice';
import { client } from '../../api/client';
import { Link } from 'react-router-dom';

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
      {/* Visual architecture banner */}
      <div className="arch-banner">
        <div className="arch-step">
          <span style={{ fontSize: '1.2rem' }}>💻</span>
          <div>
            <strong>Client</strong>
            <div style={{ fontSize: '0.75rem', opacity: 0.8 }}>X-Tenant-Id</div>
          </div>
        </div>
        <span className="arch-arrow">➔</span>
        <div className="arch-step">
          <span style={{ fontSize: '1.2rem' }}>🌐</span>
          <div>
            <strong>Global Gateway</strong>
            <div style={{ fontSize: '0.75rem', opacity: 0.8 }}>Port 8080 (MVC)</div>
          </div>
        </div>
        <span className="arch-arrow">➔</span>
        <div className="arch-step">
          <span style={{ fontSize: '1.2rem' }}>🏢</span>
          <div>
            <strong>{activePlacement ? activePlacement.region.toUpperCase() + ' App' : 'Regional App'}</strong>
            <div style={{ fontSize: '0.75rem', opacity: 0.8 }}>Ownership Check</div>
          </div>
        </div>
        <span className="arch-arrow">➔</span>
        <div className="arch-step">
          <span style={{ fontSize: '1.2rem' }}>🗄️</span>
          <div>
            <strong>{activePlacement ? activePlacement.shardId : 'Shard DB'}</strong>
            <div style={{ fontSize: '0.75rem', opacity: 0.8 }}>@TenantId Isolation</div>
          </div>
        </div>
      </div>

      {/* Tenant Selector Card */}
      <div className="card" style={{ borderLeft: '4px solid var(--primary)' }}>
        <div className="card-header">
          <h2 className="card-title">Tenant Context Switcher</h2>
          <span className="badge badge-region">Simulate Multi-Tenancy</span>
        </div>
        <p style={{ margin: '0 0 1rem 0', color: 'var(--text-muted)', fontSize: '0.95rem' }}>
          Select a tenant to observe how the Global Gateway dynamically inspects placement metadata, forwards to the authorized geographic region, and connects to that region's assigned physical shard.
        </p>
        <div style={{ display: 'flex', gap: '1rem', alignItems: 'center', flexWrap: 'wrap' }}>
          <select
            id="tenantsSelector"
            value={currentTenant}
            onChange={onTenantSelect}
            style={{ maxWidth: '420px', fontWeight: 600 }}
          >
            {tenants.map((t) => (
              <option key={t.tenantId} value={t.tenantId}>
                {t.tenantId} — [{t.region.toUpperCase()} • {t.shardId}]
              </option>
            ))}
          </select>
          <Link to="/posts" className="button" style={{ fontSize: '0.85rem', padding: '0.65rem 1.25rem' }}>
            View Tenant's Posts ➔
          </Link>
        </div>
      </div>

      {/* Diagnostics Side-by-Side Grid */}
      <div className="diagnostics-grid">
        {/* Placement Record Card */}
        <div className="card">
          <div className="card-header">
            <h3 className="card-title">Directory Placement</h3>
            {activePlacement && (
              <span className="badge" style={{ backgroundColor: 'var(--success-bg)', color: 'var(--success-text)', borderColor: 'var(--success-border)' }}>
                {activePlacement.status}
              </span>
            )}
          </div>
          {activePlacement ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.9rem' }}>
              <div className="diag-item">
                <span className="diag-label">Tenant ID</span>
                <span className="diag-val"><code>{activePlacement.tenantId}</code></span>
              </div>
              <div className="diag-item">
                <span className="diag-label">Geographic Region</span>
                <span className="diag-val" style={{ textTransform: 'uppercase' }}>
                  {activePlacement.region === 'eu' && '🇪🇺 Europe (Frankfurt)'}
                  {activePlacement.region === 'me' && '🇦🇪 Middle East (Dubai)'}
                  {activePlacement.region === 'apac' && '🇸🇬 Asia Pacific (Singapore)'}
                </span>
              </div>
              <div className="diag-item">
                <span className="diag-label">Assigned Shard</span>
                <span className="diag-val"><code>{activePlacement.shardId}</code></span>
              </div>
              <div className="diag-item">
                <span className="diag-label">Data Store</span>
                <span className="diag-val" style={{ fontSize: '0.9rem', color: 'var(--text-muted)' }}>
                  directory-db (routing metadata only)
                </span>
              </div>
            </div>
          ) : (
            <p>Loading placement record...</p>
          )}
        </div>

        {/* Live Shard Routing Diagnostics Card */}
        <div className="card" style={{ background: '#fdfefe' }}>
          <div className="card-header">
            <h3 className="card-title">Live Shard Diagnostics</h3>
            <span className="badge" style={{ backgroundColor: '#e0f2fe', color: '#0369a1', borderColor: '#bae6fd' }}>
              /internal/routing-info
            </span>
          </div>

          {routingInfo ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.9rem' }}>
              <div className="diag-item">
                <span className="diag-label">Resolved Regional Service</span>
                <span className="diag-val" style={{ color: 'var(--primary)' }}>
                  {routingInfo.region.toUpperCase()} Regional App Deployment
                </span>
              </div>
              <div className="diag-item">
                <span className="diag-label">Connected Shard Pool</span>
                <span className="diag-val">
                  <code>HikariPool-{routingInfo.region}-{routingInfo.shardId}</code>
                </span>
              </div>
              <div className="diag-item">
                <span className="diag-label">Database Identifier</span>
                <span className="diag-val">
                  <code>{routingInfo.databaseIdentifier}</code>
                </span>
              </div>
              <div className="diag-item">
                <span className="diag-label">Logical Row Isolation</span>
                <span className="diag-val" style={{ fontSize: '0.9rem', color: 'var(--success-text)' }}>
                  ✓ Hibernate 6 @TenantId active ({routingInfo.tenantId})
                </span>
              </div>
            </div>
          ) : routingError ? (
            <div style={{ color: 'var(--danger-text)', padding: '0.5rem', background: 'var(--danger-bg)', borderRadius: 'var(--radius-sm)' }}>
              <strong>Diagnostics error:</strong> {routingError}
            </div>
          ) : (
            <p style={{ color: 'var(--text-muted)' }}>Querying regional routing diagnostics...</p>
          )}
        </div>
      </div>
    </section>
  );
};