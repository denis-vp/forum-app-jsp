package repository;

import model.Post;
import org.hibernate.Transaction;
import org.hibernate.Session;
import utils.HibernateUtil;

import java.util.List;

public class PostRepository {
    public List<Post> getPosts() {
        List<Post> posts = null;

        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            posts = session.createQuery("from Post", Post.class).list();
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }

        return posts;
    }

    public List<Post> getPostsByUserId(String userId) {
        List<Post> posts = null;

        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            //noinspection JpaQlInspection
            posts = session.createQuery("from Post where user_id = :userId", Post.class)
                    .setParameter("userId", userId)
                    .list();
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }

        return posts;
    }

    public Post getPostById(String id) {
        Post post = null;

        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            post = session.get(Post.class, Long.parseLong(id));
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }

        return post;
    }

    public Long savePost(Post post) {
        Transaction transaction = null;
        Long generatedId = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            generatedId = (Long) session.save(post);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
        return generatedId;
    }

    public void updatePost(Post post) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.update(post);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    public void deletePost(String id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Post post = session.get(Post.class, Long.parseLong(id));

            // Remove association
            post.getUser().getPosts().remove(post);

            session.delete(post);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }
}
