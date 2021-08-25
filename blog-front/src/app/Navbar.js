import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useSelector } from 'react-redux';
import { selectCurrentTenant } from '../features/tenants/tenantsSlice';

export const Navbar = () => {
  const [currentTenantLbl,setCurrentTenantLbl] = useState('');
  const currentTenant = useSelector(selectCurrentTenant);
  useEffect(()=>{
      setCurrentTenantLbl(currentTenant);
  },[currentTenant]);
  return (
    <nav>
      <section>
        <div className="navHeaderDiv"><h1>Multi-tenant Blog</h1><h3>{currentTenantLbl}</h3></div>

        <div className="navContent">
          <div className="navLinks">
            <Link to="/home">Home</Link>
            <Link to="/posts">Posts</Link>
            <Link to="/addPost">Add Post</Link>
          </div>
        </div>
      </section>
    </nav>
  )
}
