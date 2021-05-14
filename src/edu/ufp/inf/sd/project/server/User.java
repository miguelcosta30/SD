package edu.ufp.inf.sd.project.server;

/**
 *
 * @author rmoreira
 */
public class User {

    private String uname;
    private String pword;
    private int credits;

    public User(String uname, String pword) {
        this.uname = uname;
        this.pword = pword;
    }

    @Override
    public String toString() {
        return "User{" + "uname= " + uname + ", pword= " + pword + ", credits = " + credits + '}';
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    /**
     * @return the uname
     */
    public String getUname() {
        return uname;
    }

    /**
     * @param uname the uname to set
     */
    public void setUname(String uname) {
        this.uname = uname;
    }

    /**
     * @return the pword
     */
    public String getPword() {
        return pword;
    }

    /**
     * @param pword the pword to set
     */
    public void setPword(String pword) {
        this.pword = pword;
    }
}
