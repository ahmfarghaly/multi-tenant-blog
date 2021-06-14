import React from 'react';
import { Link } from 'react-router-dom';

export const Navbar = () => {
  return (
    <nav>
      <section>
        <h1>Multi-tenant Blog</h1>

        <div className="navContent">
          <div className="navLinks">
            <Link to="/">Home</Link>
            <Link to="/posts">Posts</Link>
            <Link to="/addPost">Add Post</Link>
          </div>
        </div>
      </section>
    </nav>
  )
}
