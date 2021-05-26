package edu.ufp.inf.sd.project.server;

import edu.ufp.inf.sd.project.client.WorkerImpl;
import edu.ufp.inf.sd.project.client.WorkerRI;
import edu.ufp.inf.sd.rabbitmqservices._02_workqueues.consumer.Worker;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

public class JobGroupImpl implements JobGroupRI {
    private int id;
    private int credits;
    private String filename;
    private int algorithm;
    private User owner;
    private ArrayList<WorkerRI> workers = new ArrayList<>();
    //boolean schedulingState; //true if running, false if not
    private HashMap<Integer, Integer> workerMakespan = new HashMap<>();
    private SchedulingState schedulingState;

    public JobGroupImpl(User owner, int algorithm, int credits, String filename, int id) {
        this.owner = owner;
        //schedulingState = true;
        this.id = id;
        this.algorithm = algorithm;
        this.credits = credits;
        this.filename = filename;
    }
    @Override
    public void addWorker(WorkerRI worker) {
        if(!this.workers.contains(worker)) {
            this.workers.add(worker);
        }
    }


    @Override
    public void remWorker(WorkerRI workerRI) {
        this.workers.remove(workerRI);
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
                //", schedulingState=" + schedulingState +
                '}';
    }
    @Override
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

    @Override
    public String getFilename() throws RemoteException {
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

    public ArrayList<WorkerRI> getWorkers() {
        return workers;
    }

    public void setWorkers(ArrayList<WorkerRI> workers) {
        this.workers = workers;
    }

    @Override
    public SchedulingState getSchedulingState() {
        return schedulingState;
    }

    @Override
    public void setSchedulingState(SchedulingState schedulingState) throws RemoteException {
        this.schedulingState = schedulingState;
        notifyWorkers();
    }

    public void notifyWorkers() throws RemoteException {
        for(WorkerRI w : workers) {
            w.update();
        }
    }
    @Override
    public void update(int workerID) throws RemoteException {
            int maskespan = getWorker(workerID).getLastWorkerState().getMakespan();
            this.workerMakespan.put(workerID,maskespan);

            if(this.workerMakespan.size() == this.workers.size()) { //calcular o melhor makespan porque já acabou
                int idWorkerMinMake = calculateMinMakespan();
                for(WorkerRI w : this.workers) {
                    if(w.getId() == idWorkerMinMake) {
                        w.addCredits(11);
                        this.credits -=11;
                    } else {
                        w.addCredits(1);
                        this.credits--;
                    }
                }
            }
    }

    public WorkerRI getWorker(int workerID) {
        for(WorkerRI w : this.workers) {
            if(w.getId() == workerID) {
                return w;
            }
        }
        return null;
    }

    public int calculateMinMakespan() {
        int aux = Integer.MAX_VALUE;
        int wID = 0;
        for(int workerId : this.workerMakespan.keySet()) {
            if (workerMakespan.get(workerId) < aux) {
                aux = workerMakespan.get(workerId);
                wID = workerId;
            }
        }
        return wID;
    }

//public boolean isSchedulingState() {
    //   return schedulingState;
    // }
    // @Override
   // public void setSchedulingState(boolean schedulingState) {
   //     this.schedulingState = schedulingState;
   // }


}
