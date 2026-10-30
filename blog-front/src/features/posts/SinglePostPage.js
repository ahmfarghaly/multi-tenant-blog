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
      <section className="card">
        <h2>Post not found!</h2>
        <p style={{ color: 'var(--text-muted)' }}>The requested article does not exist or has been deleted.</p>
        <Link to="/posts" className="button">
          Back to Articles
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
      <article className="card" style={{ padding: '2rem' }}>
        <div className="card-header" style={{ marginBottom: '1.25rem' }}>
          <div>
            <h2 style={{ fontSize: '1.75rem', fontWeight: 700, margin: '0 0 0.5rem', color: 'var(--text-main)' }}>
              {post.title}
            </h2>
            <div className="post-meta" style={{ margin: 0 }}>
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
              {post.updatedAt && (
                <>
                  <span>•</span>
                  <span>Updated: {new Date(post.updatedAt).toLocaleDateString(undefined, {
                    year: 'numeric',
                    month: 'short',
                    day: 'numeric',
                    hour: '2-digit',
                    minute: '2-digit'
                  })}</span>
                </>
              )}
            </div>
          </div>
          <span className={`badge ${post.status === 'PUBLISHED' ? 'badge-published' : 'badge-draft'}`}>
            {post.status}
          </span>
        </div>

        <div style={{ whiteSpace: 'pre-wrap', lineHeight: '1.75', fontSize: '1.05rem', color: '#334155', minHeight: '120px', padding: '0.5rem 0 2rem' }}>
          {post.body}
        </div>

        <div style={{ borderTop: '1px solid var(--border)', paddingTop: '1.25rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Link to="/posts" className="button muted-button">
            &larr; Back to Articles
          </Link>
          <div style={{ display: 'flex', gap: '0.75rem' }}>
            <Link to={`/editPost/${post.id}`} className="button">
              Edit Article
            </Link>
            <button
              type="button"
              onClick={onDeletePost}
              className="button danger-button"
            >
              Delete Article
            </button>
          </div>
        </div>
      </article>
    </section>
  );
};