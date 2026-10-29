import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { client } from '../../api/client';

const initialState = {
  posts: [],
  status: 'idle',
  error: null,
};

export const fetchPosts = createAsyncThunk(
  'posts/fetchPosts',
  async (currentTenant) => {
    const response = await client.get('/api/posts', {
      headers: { 'X-Tenant-Id': currentTenant },
    });
    return response;
  }
);

export const addNewPost = createAsyncThunk(
  'posts/addNewPost',
  async ({ title, body, tenantId }) => {
    const response = await client.post(
      '/api/posts',
      { title, body },
      { headers: { 'X-Tenant-Id': tenantId } }
    );
    return response;
  }
);

export const updatePost = createAsyncThunk(
  'posts/updatePost',
  async ({ id, title, body, status, tenantId }) => {
    const response = await client.patch(
      `/api/posts/${id}`,
      { title, body, status },
      { headers: { 'X-Tenant-Id': tenantId } }
    );
    return response;
  }
);

export const deletePost = createAsyncThunk(
  'posts/deletePost',
  async ({ id, tenantId }) => {
    await client.delete(`/api/posts/${id}`, {
      headers: { 'X-Tenant-Id': tenantId },
    });
    return id;
  }
);

const postsSlice = createSlice({
  name: 'posts',
  initialState,
  reducers: {
    postsReset(state) {
      state.posts = [];
      state.status = 'idle';
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchPosts.pending, (state) => {
        state.status = 'loading';
      })
      .addCase(fetchPosts.fulfilled, (state, action) => {
        state.status = 'succeeded';
        state.posts = action.payload;
      })
      .addCase(fetchPosts.rejected, (state, action) => {
        state.status = 'failed';
        state.error = action.error.message;
      })
      .addCase(addNewPost.fulfilled, (state, action) => {
        state.posts.unshift(action.payload);
      })
      .addCase(updatePost.fulfilled, (state, action) => {
        const index = state.posts.findIndex((p) => p.id === action.payload.id);
        if (index !== -1) {
          state.posts[index] = action.payload;
        }
      })
      .addCase(deletePost.fulfilled, (state, action) => {
        state.posts = state.posts.filter((p) => p.id !== action.payload);
      });
  },
});

export const { postsReset } = postsSlice.actions;
export default postsSlice.reducer;

export const selectAllPosts = (state) => state.posts.posts;
export const selectPostById = (state, postId) =>
  state.posts.posts.find((post) => String(post.id) === String(postId));