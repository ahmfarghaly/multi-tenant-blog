import React, { useEffect } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { Link } from 'react-router-dom';
import { PostAuthor } from './PostAuthor';
import { TimeAgo } from './TimeAgo';
import { selectAllPosts, fetchPosts } from './postsSlice';
import { selectCurrentTenant } from '../tenants/tenantsSlice';


export const PostsList = () => {
    const dispatch = useDispatch();
    
    const posts = useSelector(selectAllPosts);
    const postsStatus = useSelector(state=>state.posts.status);
    const error = useSelector(state=>state.posts.error);
    const currentTenant = useSelector(selectCurrentTenant);

    useEffect(()=>{
        if(postsStatus==='idle') {
            dispatch(fetchPosts(currentTenant));
        }
    },[postsStatus,dispatch,currentTenant]);

    let content;
    if(postsStatus==='loading') {
        content = <div className="loader">Loading...</div>;
    } else if(postsStatus==='completed') {
        const orderedPosts = posts.slice().sort((a,b)=>b.date.localeCompare(a.date));
        content = orderedPosts.map(post=>(
            <PostExcerpt key={post.id} post={post} />
        ))
    } else if(postsStatus==='failed') {
        content=<div>{error}</div>
    }
    
    return (
        <section className="posts-list">
            <h2>Posts</h2>
            {content}
        </section>
    );
}

const PostExcerpt =(props)=>{
    //const post = props
    return (
        <article className="post-excerpt" key={props.post.id}>
            <h3>{props.post.title}</h3>
            <PostAuthor userId={props.post.user} />
            <TimeAgo timestamp={props.post.date} />
            <p className="post-content">{props.post.content.substring(0, 100)}</p>
            <Link to={`/posts/${props.post.id}`} className="button muted-button">
                View Post
            </Link>
        </article>
    )
}