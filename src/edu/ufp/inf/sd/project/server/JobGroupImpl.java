package edu.ufp.inf.sd.project.server;

import edu.ufp.inf.sd.project.client.WorkerImpl;
import edu.ufp.inf.sd.rabbitmqservices._02_workqueues.consumer.Worker;

import java.util.ArrayList;

public class JobGroupImpl implements JobGroupRI {
    private int id;
    private int credits;
    private String filename;
    private int algorithm;
    private User owner;
    private ArrayList<WorkerImpl> workers = new ArrayList<>();
    private ArrayList<Worker> workerArrayList = new ArrayList<>();
    boolean schedulingState; //true if running, false if not

    public JobGroupImpl(User owner, int algorithm, int credits, String filename, int id) {
        this.owner = owner;
        schedulingState = true;
        this.id = id;
        this.algorithm = algorithm;
        this.credits = credits;
        this.filename = filename;
    }

    public void addWorker(WorkerImpl worker) {
        if(!this.workers.contains(worker)) {
            this.workers.add(worker);
        }
    }

    @Override
    public String toString() {
        return "JobGroup{" +
                "id= " + id +
                "owner=" + owner +
                ", credits=" + credits +
                ", filename='" + filename + '\'' +
                ", algorithm=" + algorithm +
                ", workers= " + workers +
                ", schedulingState=" + schedulingState +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(int algorithm) {
        this.algorithm = algorithm;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public ArrayList<WorkerImpl> getWorkers() {
        return workers;
    }

    public void setWorkers(ArrayList<WorkerImpl> workers) {
        this.workers = workers;
    }

    public boolean isSchedulingState() {
        return schedulingState;
    }

    public void setSchedulingState(boolean schedulingState) {
        this.schedulingState = schedulingState;
    }


}
