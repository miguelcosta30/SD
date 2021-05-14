package edu.ufp.inf.sd.project.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface JobShopSessionRI extends Remote {
    boolean createJobGroup(int id, int creditos, String filename, int algorithm) throws RemoteException;
    String listJobGroup() throws RemoteException;
    void pauseJobGroup(int id) throws RemoteException;
    void deleteJobGroup(int id) throws RemoteException;
    void logout() throws RemoteException;
    void assocWorker(int idJ, int idw) throws RemoteException;
    boolean createWorker(int id) throws RemoteException;
    void printWorkers() throws  RemoteException;
    }
