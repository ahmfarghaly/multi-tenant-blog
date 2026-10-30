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
    <section id="add-post-section" className="card" style={{ marginTop: '2rem' }}>
      <div className="card-header">
        <div>
          <h2 className="card-title">Create New Article</h2>
          <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
            Post will be saved to the database shard assigned to tenant <code>{currentTenant}</code>
          </span>
        </div>
        <span className="badge badge-region">{currentTenant}</span>
      </div>
      <form>
        <div>
          <label htmlFor="postTitle">Article Title</label>
          <input
            type="text"
            id="postTitle"
            name="postTitle"
            value={title}
            onChange={onTitleChanged}
            placeholder="e.g. Scaling Regional Microservices in EMEA"
          />
        </div>
        <div>
          <label htmlFor="postContent">Article Body</label>
          <textarea
            id="postContent"
            name="postContent"
            value={body}
            onChange={onBodyChanged}
            placeholder="Write your article content here..."
            rows="6"
          />
        </div>
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '0.5rem' }}>
          <button type="button" onClick={onSavePost} disabled={!canSave}>
            {addRequestStatus === 'pending' ? 'Saving to Shard...' : 'Publish Article'}
          </button>
        </div>
      </form>
    </section>
  );
};