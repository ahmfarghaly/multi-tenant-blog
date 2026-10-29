import React, { useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { addNewPost } from './postsSlice';
import { selectCurrentTenant } from '../tenants/tenantsSlice';
import { unwrapResult } from '@reduxjs/toolkit';

export const AddPostForm = () => {
  const [title, setTitle] = useState('');
  const [body, setBody] = useState('');
  const [addRequestStatus, setAddRequestStatus] = useState('idle');

  const dispatch = useDispatch();
  const currentTenant = useSelector(selectCurrentTenant);

  const onTitleChanged = (e) => setTitle(e.target.value);
  const onBodyChanged = (e) => setBody(e.target.value);

  const canSave = [title, body, currentTenant].every(Boolean) && addRequestStatus === 'idle';

  const onSavePost = async () => {
    if (canSave) {
      try {
        setAddRequestStatus('pending');
        const resultAction = await dispatch(
          addNewPost({ title, body, tenantId: currentTenant })
        );
        unwrapResult(resultAction);
        setTitle('');
        setBody('');
      } catch (err) {
        console.error('Failed to save post: ', err);
      } finally {
        setAddRequestStatus('idle');
      }
    }
  };

  return (
    <section style={{ marginTop: '2rem', padding: '1rem', border: '1px solid #ccc', borderRadius: '4px' }}>
      <h2>Add a New Post ({currentTenant})</h2>
      <form>
        <label htmlFor="postTitle">Post Title:</label>
        <input
          type="text"
          id="postTitle"
          name="postTitle"
          value={title}
          onChange={onTitleChanged}
          placeholder="Enter post title..."
        />
        <label htmlFor="postContent">Content:</label>
        <textarea
          id="postContent"
          name="postContent"
          value={body}
          onChange={onBodyChanged}
          placeholder="Write post content..."
          rows="5"
        />
        <button type="button" onClick={onSavePost} disabled={!canSave}>
          Save Draft Post
        </button>
      </form>
    </section>
  );
};