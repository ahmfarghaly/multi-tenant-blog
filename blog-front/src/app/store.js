import { configureStore } from '@reduxjs/toolkit'

import postsReducer from '../features/posts/postsSlice'
import usersReducer from '../features/users/usersSlice'
import tenantsReducer from '../features/tenants/tenantsSlice'

export default configureStore({
  reducer: {
    posts: postsReducer,
    users: usersReducer,
    tenants: tenantsReducer
  }
})
