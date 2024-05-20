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
import validator.PostValidator;

import java.io.IOException;
import java.io.PrintWriter;

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
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String token = authHeader.substring("Bearer ".length());

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
            String postsJsonString = this.gson.toJson(postRepository.getPosts());
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
                String postJsonString = this.gson.toJson(post);
                out.print(postJsonString);
            }
            // If the path is /user/{userId}, return all posts by the user with the given ID
            else if (splits.length == 3 && splits[1].equals("user")) {
                String userId = splits[2];
                String postsJsonString = this.gson.toJson(postRepository.getPostsByUserId(userId));
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
        postRepository.savePost(post);
        resp.setStatus(HttpServletResponse.SC_CREATED);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Post post = this.gson.fromJson(req.getReader(), Post.class);
        String id = (String) req.getAttribute("id");

        if (!post.getUser().getIdString().equals(id)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        try {
            PostValidator.validatePost(post);
        } catch (PostException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        Post postFound = postRepository.getPostById(post.getIdString());
        if (postFound == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

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

        String idAttr = (String) req.getAttribute("id");
        User user = userRepository.getUserById(idAttr);
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
}