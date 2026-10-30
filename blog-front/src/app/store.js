import { configureStore } from '@reduxjs/toolkit';

import postsReducer from '../features/posts/postsSlice';
import tenantsReducer from '../features/tenants/tenantsSlice';

export default configureStore({
  reducer: {
    posts: postsReducer,
    tenants: tenantsReducer,
  },
});

