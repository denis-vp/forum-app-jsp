package servlet;

import com.google.gson.Gson;
import exception.UserException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;
import repository.UserRepository;
import utils.JwtUtil;
import utils.PasswordUtil;
import validator.UserValidator;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import static validator.UserValidator.validateUser;

@WebServlet(name = "userServlet", urlPatterns = {"/user/*"})
public class UserServlet extends HttpServlet {
    private UserRepository userRepository;
    private Gson gson;

    @Override
    public void init() {
        this.userRepository = new UserRepository();
        this.gson = new Gson();
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        String pathInfo = req.getPathInfo();
        // If the path is /login or /register, skip the JWT verification
        if (pathInfo.equals("/login") || pathInfo.equals("/register")) {
            super.service(req, resp);
            return;
        }

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

        String[] splits = pathInfo.split("/");
        // If the path is /{userId}, return the usr with the given ID
        if (splits.length == 2) {
            String userId = splits[1];
            User user = userRepository.getUserById(userId);
            // If the user is not found, return 404
            if (user == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            // Hide the password and salt
            user.setPassword(null);
            user.setSalt(null);
            String userJsonString = this.gson.toJson(user);
            out.print(userJsonString);
        }
        out.flush();

        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo.equals("/login")) {
            handleLogin(req, resp);
        } else if (pathInfo.equals("/register")) {
            handleRegistration(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = this.gson.fromJson(req.getReader(), User.class);
        String salt = PasswordUtil.generateSalt();
        String hashedPassword = PasswordUtil.hashPassword(user.getPassword(), salt);
        user.setPassword(hashedPassword);
        user.setSalt(salt);

        String id = (String) req.getAttribute("id");
        if (!user.getIdString().equals(id)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        User userFound = userRepository.getUserById(user.getIdString());
        if (userFound == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        userRepository.updateUser(user);
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

        String userId = splits[1];
        User user = userRepository.getUserById(userId);
        if (user == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String id = (String) req.getAttribute("id");
        User userFound = userRepository.getUserById(id);
        if (userFound == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if (!user.getIdString().equals(id)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        userRepository.deleteUser(userId);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User userBody = this.gson.fromJson(req.getReader(), User.class);
        String email = userBody.getEmail();
        String password = userBody.getPassword();

        User user = userRepository.getUserByEmail(email);
        if (user == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String hashedPassword = PasswordUtil.hashPassword(password, user.getSalt());
        if (!hashedPassword.equals(user.getPassword())) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String token = JwtUtil.generateToken(user.getIdString());

        user.setPassword(null);
        user.setSalt(null);

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("user", user);
        responseMap.put("token", token);

        String responseJsonString = this.gson.toJson(responseMap);
        PrintWriter out = resp.getWriter();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        out.print(responseJsonString);
        out.flush();

        resp.setStatus(HttpServletResponse.SC_OK);
    }

    private void handleRegistration(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = this.gson.fromJson(req.getReader(), User.class);

        try {
            validateUser(user);
        } catch (UserException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        User userFound = userRepository.getUserByEmail(user.getEmail());
        if (userFound != null) {
            resp.sendError(HttpServletResponse.SC_CONFLICT, "Email already exists");
            return;
        }

        String salt = PasswordUtil.generateSalt();
        String hashedPassword = PasswordUtil.hashPassword(user.getPassword(), salt);
        user.setPassword(hashedPassword);
        user.setSalt(salt);

        userRepository.saveUser(user);
        resp.setStatus(HttpServletResponse.SC_CREATED);
    }
}
