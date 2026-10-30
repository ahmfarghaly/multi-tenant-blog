import React, { useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useHistory, Link } from 'react-router-dom';
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
      <section className="card">
        <h2>Post not found!</h2>
        <p style={{ color: 'var(--text-muted)' }}>The requested article does not exist or has been deleted.</p>
        <Link to="/posts" className="button">
          Back to Articles
        </Link>
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
    <section className="card">
      <div className="card-header">
        <div>
          <h2 className="card-title">Edit Article #{postId}</h2>
          <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
            Changes will be committed to the tenant database shard
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
          />
        </div>

        <div>
          <label htmlFor="postStatus">Publication Status</label>
          <select id="postStatus" value={status} onChange={onStatusChanged}>
            <option value="DRAFT">DRAFT</option>
            <option value="PUBLISHED">PUBLISHED</option>
          </select>
        </div>

        <div>
          <label htmlFor="postContent">Article Body</label>
          <textarea
            id="postContent"
            name="postContent"
            value={body}
            onChange={onBodyChanged}
            rows="8"
          />
        </div>

        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '0.5rem' }}>
          <Link to={`/posts/${postId}`} className="button muted-button">
            Cancel
          </Link>
          <button type="button" onClick={onSavePost}>
            Save Changes
          </button>
        </div>
      </form>
    </section>
  );
};