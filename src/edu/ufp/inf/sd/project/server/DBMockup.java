package edu.ufp.inf.sd.project.server;

import java.util.ArrayList;

/**
 * This class simulates a DBMockup for managing users and books.
 *
 * @author rmoreira
 *
 */
public class DBMockup {

    private final ArrayList<User> users;// = new ArrayList();

    public DBMockup() {
        this.users = new ArrayList<>();
    }

    /**
     * This constructor creates and inits the database with some books and users.
     */

    /**
     * Registers a new user.
     * 
     * @param u username
     * @param p passwd
     */
    public boolean register(String u, String p) {
        if (getUser(u) == null) {
            users.add(new User(u, p));
            return true;
        }
        return false;
    }

    /**
     * Checks the credentials of an user.
     * 
     * @param u username
     * @param p passwd
     * @return
     */
    public boolean exists(String u, String p) {
        for (User usr : this.users) {
            if (usr.getUname().compareTo(u) == 0 && usr.getPword().compareTo(p) == 0) {
                return true;
            }
        }
        return false;
        //return ((u.equalsIgnoreCase("guest") && p.equalsIgnoreCase("ufp")) ? true : false);
    }

    public User getUser(String username) {
        for(User u : this.users) {
            if(u.getUname().compareTo(username) == 0) {
                return u;
            }
        }
        return null;
    }

    /**
     * Inserts a new book into the DigLib.
     * 
     * @param t title
     * @param a authors
     */

    /**
     * Looks up for books with given title and author keywords.
     * 
     * @param t title keyword
     * @param a author keyword
     * @return
     */

}
