package edu.ufp.inf.sd.project.server;


import edu.ufp.inf.sd.project.client.WorkerImpl;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JobShopSessionImpl implements JobShopSessionRI {
    private JobShopFactoryImpl jobShop;
    private ArrayList<JobGroupImpl> jobGroups;
    private ArrayList<WorkerImpl> workers;
    private String username;


    @Override
    public boolean createJobGroup(int id, int credits, String filename, int algorithm) throws RemoteException { //falta os créditos
        User u = this.jobShop.getDbMockup().getUser(username);
        if(JobShopServer.containsJobGroup(id) || JobShopServer.containsJSS(filename)) { //verifica se jobGroup existe no array global e JSS está a ser usada
            return false;
        }

        if(algorithm != 0 && algorithm != 1) { //0 - TS 1 - GA
            return false;
        }

        JobGroupImpl jobGroup = new JobGroupImpl(u,algorithm,credits,filename,id);
        if(this.getJobGroupId(id) == null) {
            this.jobGroups.add(jobGroup);
            JobShopServer.addJobGroup(jobGroup);
            return true; //JobGroup criado com sucesso
        }
        return false;
    }

    @Override
    public String listJobGroup() throws RemoteException  {
        return this.jobGroups.toString();
    }

    @Override
    public void pauseJobGroup(int id) throws RemoteException {
           for(JobGroupImpl j : this.jobGroups) {
               if(j.getId() == id) {
                   j.setSchedulingState(false);
               }
           }
        }

    @Override
    public void deleteJobGroup(int id) throws RemoteException {
        this.jobGroups.removeIf(jb -> jb.getId()==(id));
    }

    @Override
    public void logout() throws RemoteException {
        this.jobShop.destroySession(this.username);
    }
    @Override
    public boolean createWorker(int id) {
        if(JobShopServer.containsWorker(id)) { //verifica se jobGroup existe no array global e JSS está a ser usada
            return false;
        }

        WorkerImpl worker = new WorkerImpl(id);
        if(worker.getJobGroup() != null) { //se worker tiver jobgroup (Já pode ter sido utlizado)
            worker.getJobGroup().getWorkers().remove(worker);
        }
        if(this.getWorkerId(id) == null) {
            this.workers.add(worker);
            JobShopServer.addWorker(worker);
            return true; //JobGroup criado com sucesso
        }
        return false;
    }

    public void assocWorker(int idJ,int idw) { //id do worker id jobgroup
        JobGroupImpl j = JobShopServer.getJobGroup(idJ); //buscar o jobgroup por id
        WorkerImpl w = this.getWorkerId(idw);
            if(j != null && w != null) {
                    j.addWorker(w); //adiciona worker ao array de workers do jobgroup
                    w.setJobGroup(j); //worker fica com referencia para o seu jobgroup
            }
    }

    private JobGroupImpl getJobGroupId(int id) {
        for(JobGroupImpl j : this.jobGroups) {
            if(j.getId() == id) {
                return j;
            }
        }
        return null;
    }

    private WorkerImpl getWorkerId(int id) {
        for(WorkerImpl w :this.workers) {
            if(w.getId() == id) {
                return w;
            }
        }
        return null;
    }

    private boolean containsWorker(int id) {
        for(WorkerImpl w : this.workers) {
            if(w.getId() == id) {
                return true; //existe worker
            }
        }
        return false;
    }

    public void printWorkers() {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, JobShopServer.printWorkers());
    }


    public JobShopSessionImpl(JobShopFactoryImpl jobShopFactory, String username) throws RemoteException{
        super();
        this.jobShop = jobShopFactory;
        this.username = username;
        this.jobGroups = new ArrayList<>();
        this.workers = new ArrayList<>();
        UnicastRemoteObject.exportObject(this,0);
    }


}
