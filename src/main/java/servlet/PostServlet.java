package servlet;

import exception.PostException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Post;
import model.User;
import repository.PostRepository;
import com.google.gson.Gson;
import repository.UserRepository;
import utils.JwtUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static validator.PostValidator.validatePost;

@WebServlet(name = "postServlet", urlPatterns = {"/post/*"})
public class PostServlet extends HttpServlet {
    private UserRepository userRepository;
    private PostRepository postRepository;
    private Gson gson;

    @Override
    public void init() {
        this.userRepository = new UserRepository();
        this.postRepository = new PostRepository();
        this.gson = new Gson();
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        String token = null;
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("token")) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        if (token == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String id = JwtUtil.verifyToken(token);
        if (id == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        req.setAttribute("id", id);

        super.service(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        PrintWriter out = resp.getWriter();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        // If the path is /, return all posts
        if (pathInfo == null || pathInfo.equals("/")) {
            List<Post> posts = postRepository.getPosts();
            posts.forEach(post -> {
                post.getUser().setPosts(null);
                post.getUser().setPassword(null);
                post.getUser().setSalt(null);
                post.setComments(null);
            });
            String postsJsonString = this.gson.toJson(posts);
            out.print(postsJsonString);
        } else {
            String[] splits = pathInfo.split("/");
            // If the path is /{postId}, return the post with the given ID
            if (splits.length == 2) {
                String postId = splits[1];
                Post post = postRepository.getPostById(postId);
                // If the post is not found, return a not found status
                if (post == null) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                post.getUser().setPosts(null);
                post.getUser().setPassword(null);
                post.getUser().setSalt(null);
                post.setComments(null);
                String postJsonString = this.gson.toJson(post);
                out.print(postJsonString);
            }
            // If the path is /user/{userId}, return all posts by the user with the given ID
            else if (splits.length == 3 && splits[1].equals("user")) {
                String userId = splits[2];
                List<Post> posts = postRepository.getPostsByUserId(userId);
                posts.forEach(post -> {
                    post.getUser().setPosts(null);
                    post.setComments(null);
                    post.getUser().setPassword(null);
                    post.getUser().setSalt(null);
                });
                String postsJsonString = this.gson.toJson(posts);
                out.print(postsJsonString);
            }
            // Otherwise, return a bad request status
            else {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
        }
        out.flush();

        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Post post = this.gson.fromJson(req.getReader(), Post.class);
        String id = (String) req.getAttribute("id");

        try {
            validatePost(post);
        } catch (PostException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        User user = userRepository.getUserById(id);
        if (user == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        post.setUser(user);

        Long generatedId = postRepository.savePost(post);

        PrintWriter out = resp.getWriter();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        out.print(this.gson.toJson(generatedId));
        out.flush();

        resp.setStatus(HttpServletResponse.SC_CREATED);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // If the path is /post/report, handle the report request
        String pathInfo = req.getPathInfo();
        if (pathInfo.startsWith("/report")) {
            handleReport(req, resp);
            return;
        }

        Post post = this.gson.fromJson(req.getReader(), Post.class);
        post.setIsReported(false);

        String id = (String) req.getAttribute("id");

        try {
            validatePost(post);
        } catch (PostException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        Post postFound = postRepository.getPostById(post.getIdString());
        if (postFound == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        } else if (!postFound.getUser().getIdString().equals(id)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        post.setUser(postFound.getUser());

        postRepository.updatePost(post);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        String[] splits = pathInfo.split("/");
        if (splits.length != 2) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        String postId = splits[1];
        Post post = postRepository.getPostById(postId);
        if (post == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String id = (String) req.getAttribute("id");
        User user = userRepository.getUserById(id);
        if (user == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if (!post.getUser().getIdString().equals(user.getIdString())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        postRepository.deletePost(postId);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    private void handleReport(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        boolean isReported = Boolean.parseBoolean(req.getParameter("isReported"));
        String postId = req.getParameter("postId");

        String id = (String) req.getAttribute("id");

        Post postFound = postRepository.getPostById(postId);
        if (postFound == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        } else if (postFound.getUser().getIdString().equals(id)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        } else if (postFound.getIsReported()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        postFound.setIsReported(isReported);

        postRepository.updatePost(postFound);
        resp.setStatus(HttpServletResponse.SC_OK);
    }
}