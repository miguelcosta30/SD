package edu.ufp.inf.sd.project.client;

import edu.ufp.inf.sd.project.server.JobGroupRI;
import edu.ufp.inf.sd.project.server.SchedulingState;

import java.rmi.RemoteException;

public interface WorkerRI {
    int runTS(String jsspInstance) throws RemoteException;
    int getId();
    JobGroupRI getJobGroup() throws RemoteException;
    void setJobGroup(JobGroupRI jobGroup) throws RemoteException;
    void update() throws RemoteException;
    SchedulingState getLastWorkerState();
    void setLastWorkerState(SchedulingState lastWorkerState);
    void addCredits(int credits) throws RemoteException;
    void runGA(String jsspInstance) throws RemoteException;
    }
