import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { client } from '../../api/client';

const initialState = {
  tenants: [],
  currentTenant: 'tenant-eu-001',
  status: 'idle',
  error: null,
};

export const fetchTenants = createAsyncThunk('tenants/fetchTenants', async () => {
  const response = await client.get('/api/tenants');
  return response;
});

const tenantsSlice = createSlice({
  name: 'tenants',
  initialState,
  reducers: {
    tenantSelected(state, action) {
      state.currentTenant = action.payload;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchTenants.pending, (state) => {
        state.status = 'loading';
      })
      .addCase(fetchTenants.fulfilled, (state, action) => {
        state.status = 'succeeded';
        state.tenants = action.payload;
        if (!state.currentTenant && action.payload.length > 0) {
          state.currentTenant = action.payload[0].tenantId;
        }
      })
      .addCase(fetchTenants.rejected, (state, action) => {
        state.status = 'failed';
        state.error = action.error.message;
      });
  },
});

export const { tenantSelected } = tenantsSlice.actions;
export const selectAllTenants = (state) => state.tenants.tenants;
export const selectCurrentTenant = (state) => state.tenants.currentTenant;
export default tenantsSlice.reducer;