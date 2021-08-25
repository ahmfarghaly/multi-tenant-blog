import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { client } from '../../api/client';

const initialState = 
{
    posts: [],
    status: 'idle',
    error: null
};

export const fetchPosts = createAsyncThunk('posts/fetchPosts', async currentTenant => {
    //,{headers:{'X-TenantID':state.tenents.currentTenant}}
    const response = await client.get('/posts',{headers:{'X-TenantID':currentTenant}})
    return response.posts;
});

export const addNewPost = createAsyncThunk('posts/addNewPost', async initialPost=>{
    const response = await client.post('/posts', {post:initialPost});
    return response.post;
})

const postsSlice = createSlice({
    name: 'posts',
    initialState,
    reducers: {
        // postAdd: {
        //     reducer(state,action) {
        //         state.posts.push(action.payload);
        //     },
        //     prepare(title,content, userId) {
        //         return {
        //             payload: {
        //                 id: nanoid(),
        //                 title,
        //                 content,
        //                 user: userId,
        //                 date: new Date().toISOString()
        //             }
        //         }
        //     }
        // },
        postUpdate(state, action) {
            const { id, title, content } = action.payload;
            const existingPost = state.posts.find(post=>post.id===id);
            if (existingPost) {
                existingPost.title = title;
                existingPost.content = content;
            }
        }
    },
    extraReducers: {
        [fetchPosts.pending]: (state,action)=> {
            state.status='loading'
        },
        [fetchPosts.fulfilled]: (state,action)=> {
            state.status='completed'
            state.posts = state.posts.concat(action.payload)
        },
        [fetchPosts.rejected]: (state,action)=> {
            state.status='failed'
            state.error=action.error.message
        },
        [addNewPost.fulfilled]: (state,action)=>{
            state.posts.push(action.payload);
        }
    }
});

export const { postAdd, postUpdate } = postsSlice.actions;
export default postsSlice.reducer;

export const selectAllPosts = state => state.posts.posts;

export const selectPostById =(state,postId)=>{
    state.posts.posts.find(post=>post.id===postId);
}