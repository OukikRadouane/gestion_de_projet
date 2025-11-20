package dao;

import com.gestionprojet.model.Sprint;
import com.gestionprojet.model.Task;
import com.gestionprojet.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionBuilder;

import java.util.List;

public class TaskDAO {

    public void save(Task task) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            session.beginTransaction();
            session.persist(task);
            session.getTransaction().commit();
        } catch (Exception e) {
            if (session.getTransaction().isActive()) {
                session.getTransaction().rollback();
            }
            throw e;
        } finally {
            session.close();
        }
    }
    public void update(Task task){
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            session.beginTransaction();
            session.merge(task);
            session.getTransaction().commit();
        }catch(Exception e){
            if(session.getTransaction().isActive()){
                session.getTransaction().rollback();
            }throw e;
        }finally {
            session.close();
        }
    }
    public void delete(Task task){
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            session.beginTransaction();
            if(task != null){
                session.remove(task);
            }
            session.getTransaction().commit();
        }catch(Exception e){
            if(session.getTransaction().isActive()){
                session.getTransaction().rollback();
            }throw e;
        }finally {
            session.close();
        }
    }
    public Task getById(int id){
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            return session.find(Task.class, id);
        }finally {
            session.close();
        }
    }

    public List<Task> getAll(){
        Session session = HibernateUtil.getSessionFactory().openSession();
        try{
            return session.createQuery("SELECT t FROM Task t ORDER BY t.deadline", Task.class).list();
        }finally {
            session.close();
        }
    }
    public List<Task> getBySprint(Sprint sprint){
        Session session = HibernateUtil.getSessionFactory().openSession();
        try{
            return session.createQuery("Select t From Task t Where t.sprint= :sprint Order By t.priority DESC, t.deadline", Task.class).
            setParameter("sprint", sprint).getResultList();
        }finally {
            session.close();
        }

    }
    public List<Task> getBySprintAndStatus(Sprint sprint , String status){
        Session session = HibernateUtil.getSessionFactory().openSession();
        try{
            return session.createQuery("Select t From Task t Where t.sprint= :sprint and t.status= :status Order By t.priority DESC, t.deadline", Task.class).
                    setParameter("sprint", sprint).setParameter("status", status).getResultList();
        }finally {
            session.close();
        }

    }

}
