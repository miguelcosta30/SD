package edu.ufp.inf.sd.project.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface JobShopSessionRI extends Remote {
    boolean createJobGroup(int id, int creditos, String filename, int algorithm) throws RemoteException;
    String listJobGroup() throws RemoteException;
    void pauseJobGroup(int id) throws RemoteException;
    void deleteJobGroup(int id) throws RemoteException;
    void logout() throws RemoteException;
    boolean assocWorker(int idJ, int idw) throws RemoteException;
    void printWorkers(int id) throws  RemoteException;
    void printWorkersSession() throws RemoteException;
    void execute(int idJ, SchedulingState schedulingState) throws RemoteException;
    String showBalace() throws RemoteException;
    }
