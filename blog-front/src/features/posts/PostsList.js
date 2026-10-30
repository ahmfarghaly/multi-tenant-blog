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
    content = (
      <div className="card" style={{ textAlign: 'center', padding: '2.5rem 1rem' }}>
        <div style={{ color: 'var(--text-muted)', fontSize: '0.95rem' }}>
          Loading articles for <strong>{currentTenant}</strong> from regional shard...
        </div>
      </div>
    );
  } else if (postsStatus === 'succeeded' || postsStatus === 'idle') {
    if (posts.length === 0) {
      content = (
        <div className="card" style={{ textAlign: 'center', padding: '3rem 1.5rem' }}>
          <div style={{ fontSize: '2rem', marginBottom: '0.75rem' }}>📝</div>
          <h3 style={{ margin: '0 0 0.5rem', color: 'var(--text-main)' }}>No articles found</h3>
          <p style={{ color: 'var(--text-muted)', margin: '0 0 1.5rem', fontSize: '0.95rem' }}>
            No posts currently exist in the database for tenant <strong>{currentTenant}</strong>.
          </p>
          <a href="#add-post-section" className="button">
            Create First Article ↓
          </a>
        </div>
      );
    } else {
      content = posts.map((post) => <PostExcerpt key={post.id} post={post} />);
    }
  } else if (postsStatus === 'failed') {
    content = (
      <div className="card" style={{ borderLeft: '4px solid #ef4444', backgroundColor: 'var(--danger-bg)' }}>
        <h4 style={{ margin: '0 0 0.5rem', color: 'var(--danger-text)' }}>Failed to load posts</h4>
        <p style={{ margin: 0, color: 'var(--danger-text)', fontSize: '0.9rem' }}>{error}</p>
      </div>
    );
  }

  return (
    <section className="posts-list" style={{ marginBottom: '2.5rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
        <div>
          <h2 style={{ margin: 0, fontSize: '1.5rem', fontWeight: 700 }}>Tenant Articles</h2>
          <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
            Showing posts isolated for tenant <code>{currentTenant}</code>
          </span>
        </div>
        <span className="badge badge-region">{posts.length} {posts.length === 1 ? 'Article' : 'Articles'}</span>
      </div>
      {content}
    </section>
  );
};

const PostExcerpt = ({ post }) => {
  return (
    <article className="post-card" key={post.id}>
      <div className="post-card-header">
        <h3>{post.title}</h3>
        <span
          className={`badge ${post.status === 'PUBLISHED' ? 'badge-published' : 'badge-draft'}`}
        >
          {post.status}
        </span>
      </div>
      <div className="post-meta">
        <span>Tenant: <code>{post.tenantId}</code></span>
        <span>•</span>
        <span>
          Created: {post.createdAt ? new Date(post.createdAt).toLocaleDateString(undefined, {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
          }) : 'N/A'}
        </span>
      </div>
      <p className="post-body-preview">
        {post.body ? post.body.substring(0, 160) + (post.body.length > 160 ? '...' : '') : ''}
      </p>
      <div style={{ display: 'flex', gap: '0.75rem' }}>
        <Link to={`/posts/${post.id}`} className="button muted-button" style={{ fontSize: '0.85rem', padding: '0.45rem 0.9rem' }}>
          View Post &rarr;
        </Link>
      </div>
    </article>
  );
};