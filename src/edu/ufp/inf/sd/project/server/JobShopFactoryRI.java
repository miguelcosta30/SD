package edu.ufp.inf.sd.project.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface JobShopFactoryRI extends Remote {
    boolean register(String username, String password) throws RemoteException;
    JobShopSessionRI login(String username, String password) throws RemoteException;
    void destroySession (String u) throws RemoteException;
}
