package validator;

import exception.PostException;
import model.Post;

import java.util.ArrayList;
import java.util.List;

public class PostValidator {
    public static void validatePost(Post post) throws PostException {
        List<String> errors = new ArrayList<>();

        // Title validation
        if (post.getTitle() == null || post.getTitle().isEmpty() || post.getTitle().length() > 255) {
            errors.add("Title must be between 1 and 255 characters");
        }

        // Content validation
        if (post.getContent() == null || post.getContent().isEmpty() || post.getContent().length() > 255) {
            errors.add("Content must be between 1 and 255 characters");
        }

        if (!errors.isEmpty()) {
            throw new PostException(String.join(", ", errors));
        }
    }
}
