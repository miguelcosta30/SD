package edu.ufp.inf.sd.project.server;

import edu.ufp.inf.sd.project.client.WorkerRI;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface JobGroupRI extends Remote {
     void addWorker(WorkerRI worker);
     int getId();
     ArrayList<WorkerRI> getWorkers();
     SchedulingState getSchedulingState() throws RemoteException;
     void setSchedulingState(SchedulingState state) throws RemoteException;
     void update(int workerID) throws RemoteException;
     String getFilename() throws RemoteException;
     void waitforWorker(String []argv) throws RemoteException;
     void updateRMQ() throws RemoteException;
}
