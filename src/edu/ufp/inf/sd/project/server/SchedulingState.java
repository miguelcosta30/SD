package edu.ufp.inf.sd.project.server;

import java.io.Serializable;



public class SchedulingState implements Serializable {
    private String instace;
    private int credits;
    private int makespan;

    public int getMakespan() {
        return makespan;
    }

    public void setMakespan(int makespan) {
        this.makespan = makespan;
    }

    public String getInstace() {
        return instace;
    }

    public void setInstace(String instace) {
        this.instace = instace;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public SchedulingState(String instace) {
        this.instace = instace;
    }
}
