import React, { useState, useEffect } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useSelector } from 'react-redux';
import { selectCurrentTenant } from '../features/tenants/tenantsSlice';

export const Navbar = () => {
  const currentTenant = useSelector(selectCurrentTenant);
  const [currentTenantLbl, setCurrentTenantLbl] = useState('');
  const location = useLocation();

  useEffect(() => {
    setCurrentTenantLbl(currentTenant);
  }, [currentTenant]);

  const isActive = (path) => location.pathname === path;

  return (
    <nav>
      <div className="navContainer">
        <Link to="/home" className="brand">
          <h1>Regional Multi-Tenant SaaS</h1>
          <span className="brand-badge">Demo</span>
        </Link>

        <div className="navRight">
          <div className="navLinks">
            <Link
              to="/home"
              style={{
                color: isActive('/home') ? '#ffffff' : '#94a3b8',
                backgroundColor: isActive('/home') ? '#1e293b' : 'transparent',
              }}
            >
              Topology & Placement
            </Link>
            <Link
              to="/posts"
              style={{
                color: isActive('/posts') ? '#ffffff' : '#94a3b8',
                backgroundColor: isActive('/posts') ? '#1e293b' : 'transparent',
              }}
            >
              Blog Posts
            </Link>
            <Link
              to="/addPost"
              style={{
                color: isActive('/addPost') ? '#ffffff' : '#94a3b8',
                backgroundColor: isActive('/addPost') ? '#1e293b' : 'transparent',
              }}
            >
              + Create Post
            </Link>
          </div>

          {currentTenantLbl && (
            <div className="active-tenant-chip" title="Current tenant context sent in X-Tenant-Id header">
              <span className="dot"></span>
              <span>Tenant: <strong>{currentTenantLbl}</strong></span>
            </div>
          )}
        </div>
      </div>
    </nav>
  );
};
