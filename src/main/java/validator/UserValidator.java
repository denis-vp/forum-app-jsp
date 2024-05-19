package validator;

import exception.UserException;
import model.User;

import java.util.ArrayList;
import java.util.List;

public class UserValidator {
    public static void validateUser(User user) throws UserException {
        List<String> errors = new ArrayList<>();

        // Username validation
        if (user.getUsername() == null || user.getUsername().length() < 3 || user.getUsername().length() > 32) {
            errors.add("Username must be between 3 and 32 characters");
        }
        if (!user.getUsername().matches("^[a-zA-Z0-9_]*$")) {
            errors.add("Username must contain only letters, numbers, and underscores");
        }

        // Email validation
        if (user.getEmail() == null || user.getEmail().length() < 3 || user.getEmail().length() > 32) {
            errors.add("Email must be between 3 and 32 characters");
        }
        if (!user.getEmail().matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")) {
            errors.add("Invalid email format");
        }

        // Password validation
        if (user.getPassword() == null || user.getPassword().length() < 8 || user.getPassword().length() > 32) {
            errors.add("Password must be between 8 and 32 characters");
        }
        if (!user.getPassword().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$")) {
            errors.add("Password must contain at least one uppercase letter, one lowercase letter, and one number");
        }

        if (!errors.isEmpty()) {
            throw new UserException(String.join(", ", errors));
        }
    }
}
