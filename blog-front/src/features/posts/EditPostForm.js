import React, { useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { useHistory } from 'react-router-dom'

import { postUpdate, selectPostById } from './postsSlice'

export const EditPostForm =({ match })=>{
    const { postId } = match.params;
    const post = useSelector(state=>selectPostById(state,postId));

    const [title, setTitle] = useState(post.title);
    const [content, setContent] = useState(post.content);

    const onTitleChanged = e => setTitle(e.target.value);
    const onContentChanged = e => setContent(e.target.value);

    const dispatch = useDispatch();
    const history = useHistory();

    const onSavePost=()=>{
        if(title && content) {
            dispatch(postUpdate({id:postId,title,content}));
            history.push(`/posts/${post.id}`);
        }
    }

    return(
        <section>
            <h2>Edit Post</h2>
            <form>
                <label htmlFor="postTitle">Post Title:</label>
                <input
                    type="text"
                    id="postTitle"
                    name="postTitle"
                    placeholder="What's on your mind?"
                    value={title}
                    onChange={onTitleChanged}
                />
                <label htmlFor="postContent">Content:</label>
                <textarea
                    id="postContent"
                    name="postContent"
                    value={content}
                    onChange={onContentChanged}
                />
            </form>
            <button type="button" onClick={onSavePost}>
                Save Post
            </button>
        </section>
    );
}