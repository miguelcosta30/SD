package edu.ufp.inf.sd.project.server;


import edu.ufp.inf.sd.project.client.WorkerImpl;
import edu.ufp.inf.sd.project.client.WorkerRI;
import edu.ufp.inf.sd.project.producer.Producer;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JobShopSessionImpl implements JobShopSessionRI {
    private JobShopFactoryImpl jobShop;
    private ArrayList<JobGroupRI> jobGroups;
    private ArrayList<WorkerRI> workers;
    private String username;

    @Override
    public boolean createJobGroup(int id, int credits, String filename, int algorithm) throws RemoteException { //falta os créditos
        User u = this.jobShop.getDbMockup().getUser(username);
        if(JobShopServer.containsJobGroup(id) || JobShopServer.containsJSS(filename)) { //verifica se jobGroup existe no array global e JSS está a ser usada
            return false;
        }

        if(credits <= 0) {
            return false;
        }

        if(!this.jobShop.creditTrasaction(this.username, -credits)) {
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

        }

    @Override
    public void deleteJobGroup(int id) throws RemoteException {
        this.jobGroups.removeIf(jb -> jb.getId()==(id));
        JobShopServer.deleteJobGroup(id);
    }

    @Override
    public void logout() throws RemoteException {
        this.jobShop.destroySession(this.username);
    }

    @Override
    public String showBalace() throws RemoteException {
        User u = this.jobShop.getDbMockup().getUser(this.username);
        return u.getUname() + " " + u.getCredits();
    }
    @Override
    public void printWorkers(int id) throws RemoteException {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO,JobShopServer.printWorkers(id));
    }

    @Override
    public boolean assocWorker(int idJ) throws RemoteException { //id do worker id jobgroup
        int idw = JobShopServer.count();
        if(JobShopServer.containsWorker(idw)) { //verifica se id deste worker está ser utilizado
            return false;
         }

        JobGroupImpl j = JobShopServer.getJobGroup(idJ); //buscar o jobgroup por id
        if(j !=  null) {
            if (j.getCredits() - this.calculateMaxCreditsSentJobGroup(idJ) < 0) { //verificar se o valor de creditos do jobgrou cobre o dos workers
                Logger.getLogger(this.getClass().getName()).log(Level.WARNING,"WorkerID: " + (idw) + " " + (this.calculateMaxCreditsSentJobGroup(idJ)));
                return false;
            }
        }

        WorkerImpl worker = new WorkerImpl(idw);
        if(worker.getJobGroup() != null) { //se worker tiver jobgroup (Já pode ter sido utlizado)
            worker.getJobGroup().getWorkers().remove(worker);
        }

        if(this.getWorkerId(idw) == null) {
            this.workers.add(worker);
        }

            if(j != null) {
                    j.addWorker(worker); //adiciona worker ao array de workers do jobgroup
                    worker.setJobGroup(j); //worker fica com referencia para o seu jobgroup
                return false;
            }
            return true;
    }

    private JobGroupRI getJobGroupId(int id) {
        for(JobGroupRI j : this.jobGroups) {
            if(j.getId() == id) {
                return j;
            }
        }
        return null;
    }

    private WorkerRI getWorkerId(int id) {
        for(WorkerRI w :this.workers) {
            if(w.getId() == id) {
                return w;
            }
        }
        return null;
    }

    private boolean containsWorker(int id) {
        for(WorkerRI w : this.workers) {
            if(w.getId() == id) {
                return true; //existe worker
            }
        }
        return false;
    }

    @Override
    public void printWorkersSession() {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, this.workers.toString());
    }

    public JobShopSessionImpl(JobShopFactoryImpl jobShopFactory, String username) throws RemoteException{
        super();
        this.jobShop = jobShopFactory;
        this.username = username;
        this.jobGroups = new ArrayList<>();
        this.workers = new ArrayList<>();
        UnicastRemoteObject.exportObject(this,0);
    }

    @Override
    public void execute(int idJ, SchedulingState schedulingState) throws RemoteException {
        JobGroupRI j = this.getJobGroupId(idJ);
        if(j != null) {
            j.setSchedulingState(schedulingState);
        }
    }

    @Override
    public void executeJobGroup(int idJ) throws RemoteException {
        JobGroupRI j = this.getJobGroupId(idJ);
        if(j != null) {
            for(WorkerRI w : workers) {
                String[] args = new String[]{ "bash","-c", "/home/tiago/IdeaProjects/SD/src/edu/ufp/inf/sd/project/runscripts_rmq/runproducer.sh " + w.getId() +  " " + j.getFilename() + " "};
                try {
                    ProcessBuilder p = new ProcessBuilder("./runproducer.sh",String.valueOf(w.getId()),j.getFilename());
                    p.directory(new File("/home/tiago/IdeaProjects/SD/src/edu/ufp/inf/sd/project/runscripts_rmq"));
                    Process proc = p.start();
                    //System.out.println(proc);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
     }

    private int calculateMaxCreditsSentJobGroup(int idJobGroup) {
        int maxCredits = 0;
        int i = 0;
        JobGroupImpl j = JobShopServer.getJobGroup(idJobGroup);
        if (j != null) {
            if (!j.getWorkers().isEmpty()) {
                for (WorkerRI w : j.getWorkers()) {
                    if (i == 0) {
                        maxCredits += 11;
                        i = 1;
                    } else {
                        maxCredits += 1;
                    }
                }
                return maxCredits + 1; //para o que estou associar
            } else {
                return maxCredits + 11; //primeiro então vai ser o melhor tempo
            }
        }
        return 0;
    }



}
