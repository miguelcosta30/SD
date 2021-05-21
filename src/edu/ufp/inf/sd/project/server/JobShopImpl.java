package edu.ufp.inf.sd.project.server;


import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;



public class JobShopImpl extends UnicastRemoteObject implements JobShopRI {

    public JobShopImpl() throws RemoteException {
        super();
    }



}
