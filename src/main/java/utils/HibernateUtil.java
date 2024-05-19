package utils;

import io.github.cdimascio.dotenv.Dotenv;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {
    private static final SessionFactory sessionFactory;

    static {
        try {
            Dotenv dotenv = Dotenv.load();

            Configuration configuration = new Configuration();
            configuration.setProperty("hibernate.connection.url", dotenv.get("DB_URL"));
            configuration.setProperty("hibernate.connection.username", dotenv.get("DB_USERNAME"));
            configuration.setProperty("hibernate.connection.password", dotenv.get("DB_PASSWORD"));
            configuration.configure();

            sessionFactory = configuration.buildSessionFactory();
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}