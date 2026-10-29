import React, { useEffect } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { Link } from 'react-router-dom';
import { selectAllPosts, fetchPosts } from './postsSlice';
import { selectCurrentTenant } from '../tenants/tenantsSlice';

export const PostsList = () => {
  const dispatch = useDispatch();
  const posts = useSelector(selectAllPosts);
  const postsStatus = useSelector((state) => state.posts.status);
  const error = useSelector((state) => state.posts.error);
  const currentTenant = useSelector(selectCurrentTenant);

  useEffect(() => {
    if (currentTenant) {
      dispatch(fetchPosts(currentTenant));
    }
  }, [currentTenant, dispatch]);

  let content;
  if (postsStatus === 'loading') {
    content = <div className="loader">Loading posts for {currentTenant}...</div>;
  } else if (postsStatus === 'succeeded' || postsStatus === 'idle') {
    if (posts.length === 0) {
      content = <p>No posts found for tenant <strong>{currentTenant}</strong>. Create one below!</p>;
    } else {
      content = posts.map((post) => <PostExcerpt key={post.id} post={post} />);
    }
  } else if (postsStatus === 'failed') {
    content = <div style={{ color: 'red' }}>Error loading posts: {error}</div>;
  }

  return (
    <section className="posts-list">
      <h2>Tenant Posts ({currentTenant})</h2>
      {content}
    </section>
  );
};

const PostExcerpt = ({ post }) => {
  return (
    <article className="post-excerpt" key={post.id} style={{ marginBottom: '1.2rem', padding: '1rem', border: '1px solid #ddd', borderRadius: '4px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h3 style={{ margin: 0 }}>{post.title}</h3>
        <span
          style={{
            padding: '0.2rem 0.5rem',
            borderRadius: '4px',
            fontSize: '0.8rem',
            backgroundColor: post.status === 'PUBLISHED' ? '#28a745' : '#ffc107',
            color: post.status === 'PUBLISHED' ? '#fff' : '#000',
          }}
        >
          {post.status}
        </span>
      </div>
      <p style={{ color: '#666', fontSize: '0.85rem' }}>
        Tenant: <code>{post.tenantId}</code> | Created: {post.createdAt ? new Date(post.createdAt).toLocaleString() : 'N/A'}
      </p>
      <p className="post-content">
        {post.body ? post.body.substring(0, 150) + (post.body.length > 150 ? '...' : '') : ''}
      </p>
      <Link to={`/posts/${post.id}`} className="button muted-button">
        View Post
      </Link>
    </article>
  );
};