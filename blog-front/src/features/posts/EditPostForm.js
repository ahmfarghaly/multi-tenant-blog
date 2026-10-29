import React, { useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useHistory } from 'react-router-dom';
import { updatePost, selectPostById } from './postsSlice';
import { selectCurrentTenant } from '../tenants/tenantsSlice';

export const EditPostForm = ({ match }) => {
  const { postId } = match.params;
  const post = useSelector((state) => selectPostById(state, postId));
  const currentTenant = useSelector(selectCurrentTenant);

  const [title, setTitle] = useState(post ? post.title : '');
  const [body, setBody] = useState(post ? post.body : '');
  const [status, setStatus] = useState(post ? post.status : 'DRAFT');

  const dispatch = useDispatch();
  const history = useHistory();

  if (!post) {
    return (
      <section>
        <h2>Post not found!</h2>
      </section>
    );
  }

  const onTitleChanged = (e) => setTitle(e.target.value);
  const onBodyChanged = (e) => setBody(e.target.value);
  const onStatusChanged = (e) => setStatus(e.target.value);

  const onSavePost = async () => {
    if (title && body) {
      await dispatch(
        updatePost({
          id: postId,
          title,
          body,
          status,
          tenantId: currentTenant,
        })
      );
      history.push(`/posts/${postId}`);
    }
  };

  return (
    <section>
      <h2>Edit Post</h2>
      <form>
        <label htmlFor="postTitle">Post Title:</label>
        <input
          type="text"
          id="postTitle"
          name="postTitle"
          value={title}
          onChange={onTitleChanged}
        />

        <label htmlFor="postStatus">Status:</label>
        <select id="postStatus" value={status} onChange={onStatusChanged}>
          <option value="DRAFT">DRAFT</option>
          <option value="PUBLISHED">PUBLISHED</option>
        </select>

        <label htmlFor="postContent">Content:</label>
        <textarea
          id="postContent"
          name="postContent"
          value={body}
          onChange={onBodyChanged}
          rows="6"
        />
      </form>
      <button type="button" onClick={onSavePost}>
        Save Post Changes
      </button>
    </section>
  );
};