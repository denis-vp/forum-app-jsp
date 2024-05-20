package servlet;

import com.google.gson.Gson;
import exception.CommentException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Comment;
import model.Post;
import model.User;
import repository.CommentRepository;
import repository.PostRepository;
import repository.UserRepository;
import utils.JwtUtil;

import java.io.IOException;
import java.io.PrintWriter;

import static validator.CommentValidator.validateComment;

@WebServlet(name = "commentServlet", urlPatterns = {"/comment/*"})
public class CommentServlet extends HttpServlet {
    private UserRepository userRepository;
    private PostRepository postRepository;
    private CommentRepository commentRepository;
    private Gson gson;

    @Override
    public void init() {
        this.userRepository = new UserRepository();
        this.postRepository = new PostRepository();
        this.commentRepository = new CommentRepository();
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

        // If the path is /, return all comments
        if (pathInfo == null || pathInfo.equals("/")) {
            String commentsJsonString = this.gson.toJson(commentRepository.getComments());
            out.print(commentsJsonString);
        } else {
            String[] splits = pathInfo.split("/");
            // If the path is /{commentId}, return the comment with the given ID
            if (splits.length == 2) {
                String commentId = splits[1];
                Comment comment = commentRepository.getCommentById(commentId);
                // If the comment is not found, return a not found status
                if (comment == null) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                String commentJsonString = this.gson.toJson(comment);
                out.print(commentJsonString);
            }
            // If the path is /post/{postId}, return all comments for the post with the given ID
            else if (splits.length == 3 && splits[1].equals("post")) {
                String postId = splits[2];
                String commentsJsonString = this.gson.toJson(commentRepository.getCommentsByPostId(postId));
                out.print(commentsJsonString);
            }
            // If the path is /user/{userId}, return all comments by the user with the given ID
            else if (splits.length == 3 && splits[1].equals("user")) {
                String userId = splits[2];
                String commentsJsonString = this.gson.toJson(commentRepository.getCommentsByUserId(userId));
                out.print(commentsJsonString);
            }
            // Otherwise, return a bad request status
            else {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
        }
        out.flush();

        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Comment comment = this.gson.fromJson(req.getReader(), Comment.class);
        String id = (String) req.getAttribute("id");

        try {
            validateComment(comment);
        } catch (CommentException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        User user = userRepository.getUserById(id);
        if (user == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        Post post = postRepository.getPostById(comment.getPost().getIdString());
        if (post == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        comment.setUser(user);
        comment.setPost(post);

        commentRepository.saveComment(comment);
        resp.setStatus(HttpServletResponse.SC_CREATED);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Comment comment = this.gson.fromJson(req.getReader(), Comment.class);
        String id = (String) req.getAttribute("id");

        try {
            validateComment(comment);
        } catch (CommentException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        Comment commentFound = commentRepository.getCommentById(comment.getIdString());
        if (commentFound == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        } else if (!commentFound.getUser().getIdString().equals(id)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        comment.setUser(commentFound.getUser());
        comment.setPost(commentFound.getPost());

        commentRepository.updateComment(comment);
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

        String commentId = splits[1];
        Comment comment = commentRepository.getCommentById(commentId);
        if (comment == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String idAttr = (String) req.getAttribute("id");
        User user = userRepository.getUserById(idAttr);
        if (user == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if (!comment.getUser().getIdString().equals(user.getIdString())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        commentRepository.deleteComment(commentId);
        resp.setStatus(HttpServletResponse.SC_OK);
    }
}
