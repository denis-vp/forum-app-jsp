import model.User;
import repository.UserRepository;

// This serves for testing purposes
public class Main {
    public static void main(String[] args) {
        User user = new User();
        user.setUsername("john_doe");
        user.setEmail("john.doe@gmail.com");
        user.setPassword("password");
        user.setSalt("salt");

        UserRepository userRepository = new UserRepository();
        userRepository.saveUser(user);
    }
}
