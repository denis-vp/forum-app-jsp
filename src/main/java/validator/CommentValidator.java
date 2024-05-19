package validator;

import exception.CommentException;
import model.Comment;

import java.util.ArrayList;
import java.util.List;

public class CommentValidator {
    public static void validateComment(Comment comment) throws CommentException {
        List<String> errors = new ArrayList<>();

        // Content validation
        if (comment.getContent() == null || comment.getContent().isEmpty() || comment.getContent().length() > 255) {
            errors.add("Content must be between 1 and 255 characters");
        }

        if (!errors.isEmpty()) {
            throw new CommentException(String.join(", ", errors));
        }
    }
}
