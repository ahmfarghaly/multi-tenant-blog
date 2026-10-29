import React from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { Link, useHistory } from 'react-router-dom';
import { selectPostById, deletePost } from './postsSlice';
import { selectCurrentTenant } from '../tenants/tenantsSlice';

export const SinglePostPage = ({ match }) => {
  const { postId } = match.params;
  const dispatch = useDispatch();
  const history = useHistory();
  const post = useSelector((state) => selectPostById(state, postId));
  const currentTenant = useSelector(selectCurrentTenant);

  if (!post) {
    return (
      <section>
        <h2>Post not found!</h2>
        <Link to="/posts" className="button">
          Back to Posts List
        </Link>
      </section>
    );
  }

  const onDeletePost = async () => {
    if (window.confirm('Are you sure you want to delete this post?')) {
      await dispatch(deletePost({ id: post.id, tenantId: currentTenant }));
      history.push('/posts');
    }
  };

  return (
    <section>
      <article className="post">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <h2>{post.title}</h2>
          <span
            style={{
              padding: '0.2rem 0.6rem',
              borderRadius: '4px',
              fontSize: '0.85rem',
              backgroundColor: post.status === 'PUBLISHED' ? '#28a745' : '#ffc107',
              color: post.status === 'PUBLISHED' ? '#fff' : '#000',
            }}
          >
            {post.status}
          </span>
        </div>
        <p style={{ color: '#666', fontSize: '0.85rem' }}>
          Tenant: <code>{post.tenantId}</code> | Created: {post.createdAt ? new Date(post.createdAt).toLocaleString() : 'N/A'} | Updated: {post.updatedAt ? new Date(post.updatedAt).toLocaleString() : 'N/A'}
        </p>
        <p className="post-content" style={{ whiteSpace: 'pre-wrap', lineHeight: '1.6' }}>
          {post.body}
        </p>
        <div style={{ marginTop: '1.5rem', display: 'flex', gap: '0.8rem' }}>
          <Link to={`/editPost/${post.id}`} className="button">
            Edit Post
          </Link>
          <button
            type="button"
            onClick={onDeletePost}
            style={{ backgroundColor: '#dc3545', color: '#fff', border: 'none', padding: '0.5rem 1rem', borderRadius: '4px', cursor: 'pointer' }}
          >
            Delete Post
          </button>
        </div>
      </article>
    </section>
  );
};