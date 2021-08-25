import React, { useEffect, useState } from 'react'
import { useSelector, useDispatch } from 'react-redux'
import { selectAllTenants, selectCurrentTenant, tenantSelected } from './tenantsSlice';

export const TenantHomePage =()=>{

    const dispatch = useDispatch();
    const tenants = useSelector(selectAllTenants);
    const currentTenant = useSelector(selectCurrentTenant);
    const [currentTenantLbl,setCurrentTenantLbl] = useState('');
  
    const onTenantSelect =e => {
        setCurrentTenantLbl(e.target.value);
        dispatch(tenantSelected(e.target.value));
    }

    const tenantOptions = tenants.map(t => (
      <option key={t.name} value={t.name}>
        {t.name}
      </option>
    ));

    return (
        <section>
            <h3>Current Tenant</h3>
            <h4>{currentTenantLbl}</h4>
            <select id='tenantsSelector' value={currentTenant} onChange={onTenantSelect}>
                {tenantOptions}
            </select>
        </section>
    );
}