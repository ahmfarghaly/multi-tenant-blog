import React, { Fragment } from 'react';
import {
  BrowserRouter as Router,
  Routes,
  Route,
  Navigate,
} from 'react-router-dom';

import { Navbar } from './app/Navbar';
import { PostsList } from './features/posts/PostsList';
import { AddPostForm } from './features/posts/AddPostForm';
import { SinglePostPage } from './features/posts/SinglePostPage';
import { EditPostForm } from './features/posts/EditPostForm';
import { TenantHomePage } from './features/tenants/TenantHomePage';

function App() {
  return (
    <Router>
      <Navbar />
      <div className="App">
        <Routes>
          <Route
            path="/posts"
            element={
              <Fragment>
                <PostsList />
                <AddPostForm />
              </Fragment>
            }
          />
          <Route path="/posts/:postId" element={<SinglePostPage />} />
          <Route path="/editPost/:postId" element={<EditPostForm />} />
          <Route path="/addPost" element={<AddPostForm />} />
          <Route path="/home" element={<TenantHomePage />} />
          <Route path="*" element={<Navigate to="/home" replace />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;

