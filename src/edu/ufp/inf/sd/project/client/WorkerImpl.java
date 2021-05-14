package edu.ufp.inf.sd.project.client;

import edu.ufp.inf.sd.project.server.JobGroupImpl;
import edu.ufp.inf.sd.project.server.JobGroupRI;

import java.io.Serializable;

public class WorkerImpl implements Serializable, WorkerRI {
    private int id;
    private int credits;
    private JobGroupImpl jobGroup;
    private JobGroupRI jobGroupRI;

    public WorkerImpl(int id) {
        this.id = id;
        this.credits = 0;

    }

    public void setJobGroup(JobGroupImpl jobGroup) {
        this.jobGroup = jobGroup;
    }

    public JobGroupImpl getJobGroup() {
        return this.jobGroup;
    }

    public int getId(){
        return this.id;
    }
    @Override
    public String toString() {
        return "Worker{" +
                "id=" + id +
                ", credits=" + credits +
                ",jobgroup " + jobGroup.getId() +
                '}';
    }
}
