import React, { Fragment } from 'react';
import {
  BrowserRouter as Router,
  Switch,
  Route,
  Redirect,
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
        <Switch>
          <Route
            exact
            path="/posts"
            render={() => (
              <Fragment>
                <PostsList />
                <AddPostForm />
              </Fragment>
            )}
          />
          <Route exact path="/posts/:postId" component={SinglePostPage} />
          <Route exact path="/editPost/:postId" component={EditPostForm} />
          <Route exact path="/addPost" component={AddPostForm} />
          <Route exact path="/home" component={TenantHomePage} />
          <Redirect to="/home" />
        </Switch>
      </div>
    </Router>
  )
}

export default App
