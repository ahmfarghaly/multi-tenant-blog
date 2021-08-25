import { createSlice, nanoid, createAsyncThunk } from '@reduxjs/toolkit';
import { client } from '../../api/client';

const initialState = {
    tenants: [],
    currentTenant: ''
};

export const fetchTenants = createAsyncThunk('tenants/fetchTenants', async()=>{
    const response = await client.get('/tenants');
    console.log('Tenants: '+response);
    return response;
});

const tenantsSlice = createSlice({
    name: 'tenants',
    initialState,
    reducers: {
        tenantSelected(state,action) {
            state.currentTenant=action.payload;
        }
    },
    extraReducers: {
        [fetchTenants.fulfilled]: (state,action)=> {
            console.log(action.payload);
            state.tenants.push(...action.payload);
        }
    }
});


export const { tenantSelected } = tenantsSlice.actions;
export const selectAllTenants = state => state.tenants.tenants;
export const selectCurrentTenant = state => state.tenants.currentTenant;
export default tenantsSlice.reducer;