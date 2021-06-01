package edu.ufp.inf.sd.project.server;

import edu.ufp.inf.sd.project.client.WorkerImpl;
import edu.ufp.inf.sd.project.client.WorkerRI;
import java.io.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JobShopSessionImpl implements JobShopSessionRI {
    private JobShopFactoryImpl jobShop;
    private ArrayList<JobGroupRI> jobGroups;
    private ArrayList<WorkerRI> workers;
    private String username;

    /**
     * Cria um JobGroup
     * @param id Id do JobGroup
     * @param credits Creditos para o Jobgroup
     * @param filename Instance para o JobGroup (APENAS UTILIZADO NO RMQ)
     * @param algorithm Algoritmo a utilizar
     * @return true - criado | false - não criado
     * @throws RemoteException
     */
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

    /**
     * Lista os jobgroups
     * @return retorna string para cliente
     * @throws RemoteException
     */
    @Override
    public String listJobGroup() throws RemoteException  {
        return this.jobGroups.toString();
    }

    /**
     *
     * @param id
     * @throws RemoteException
     */
    @Override
    public void pauseJobGroup(int id) throws RemoteException {

        }

    /**
     * Apaga um JobGroup
     * @param id id jobgroup para apagar
     * @throws RemoteException
     */
    @Override
    public void deleteJobGroup(int id) throws RemoteException {
        this.jobGroups.removeIf(jb -> jb.getId()==(id));
        JobShopServer.deleteJobGroup(id);
    }

    /**
     * logou do user
     * @throws RemoteException
     */
    @Override
    public void logout() throws RemoteException {
        this.jobShop.destroySession(this.username);
    }

    /**
     * Balance do User
     * @return String de balance
     * @throws RemoteException
     */
    @Override
    public String showBalace() throws RemoteException {
        User u = this.jobShop.getDbMockup().getUser(this.username);
        return u.getUname() + " " + u.getCredits();
    }

    /**
     * Chama a funcao do Server para mostrar todos workers
     * @param id id JObgroup
     * @throws RemoteException
     */
    @Override
    public String printWorkers(int id) throws RemoteException {
        return JobShopServer.printWorkers(id);
    }

    /**
     * Associa um Worker a um jobgroup, fazendo as validações necessãrias
     * @param idJ Id Jobgroup a associar worker
     * @return true - associado | false - nao associado
     * @throws RemoteException
     */
    @Override
    public boolean assocWorker(int idJ) throws RemoteException { //id do worker id jobgroup
        int idw = JobShopServer.count();
        if(JobShopServer.containsWorker(idw)) { //verifica se id deste worker está ser utilizado
            return false;
         }

        JobGroupImpl j = JobShopServer.getJobGroup(idJ); //buscar o jobgroup por id
        if(j !=  null) {
            if (j.getCredits() - this.calculateMaxCreditsSentJobGroup(idJ) < 0) { //verificar se o valor de creditos do jobgrou cobre o dos workers
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

    /**
     * procura jobgroup na lista de jobgroups por id
     * @param id id jobgroup
     * @return jobgroup ou null
     */
    private JobGroupRI getJobGroupId(int id) {
        for(JobGroupRI j : this.jobGroups) {
            if(j.getId() == id) {
                return j;
            }
        }
        return null;
    }

    /**
     * procura worker na lista de worker por id
     * @param id id worker
     * @return worker ou null
     */
    private WorkerRI getWorkerId(int id) {
        for(WorkerRI w :this.workers) {
            if(w.getId() == id) {
                return w;
            }
        }
        return null;
    }

    /**
     * verifica se worker existe
     * @param id id worker
     * @return true existe false não existe
     */
    private boolean containsWorker(int id) {
        for(WorkerRI w : this.workers) {
            if(w.getId() == id) {
                return true; //existe worker
            }
        }
        return false;
    }

    /**
     * print workers na sessao
     */
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

    /**
     * executa RMI
     * @param idJ id jobgroup
     * @param schedulingState estado com ficheiro
     * @throws RemoteException
     */
    @Override
    public void execute(int idJ, SchedulingState schedulingState) throws RemoteException {
        if(schedulingState.getInstace().isEmpty()) {
            return;
        }
        JobGroupRI j = this.getJobGroupId(idJ);
        if(j != null) {
            j.setSchedulingState(schedulingState);
        }

    }

    /**
     * executa rabbitMQ
     * @param idJ idJobgroup
     * @throws RemoteException
     */
    @Override
    public void executeRMQ(int idJ) throws RemoteException {
        JobGroupRI j = this.getJobGroupId(idJ);
        ArrayList<Process> processes = new ArrayList<>();
        String pathaux = System.getProperty("user.dir");
        String path = pathaux.replace("out/production/SD","src/edu/ufp/inf/sd/project/logs/");
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(path + "makespanWorker.txt"));
            BufferedWriter writer2 = new BufferedWriter(new FileWriter( path + "workersInstanceAndID.txt"));
            writer2.write("");
            writer.write("");
            writer2.close();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(j != null) {
            for(WorkerRI w : j.getWorkers()) {
                String[] args = {"localhost","5672","project_logs_exchange",w.getId()+"_results"};
                try {
                    ProcessBuilder p = new ProcessBuilder("./runproducer.sh",String.valueOf(w.getId()),j.getFilename()); //correr script producer (Passar caminho do ficheiro para a fila do worker)
                    p.directory(new File("/home/tiago/IdeaProjects/SD/src/edu/ufp/inf/sd/project/runscripts_rmq"));
                    Process proc = p.start();
                    processes.add(proc);
                    j.waitforWorker(args);
                    //proc.destroy();
                    //Process pro = Runtime.getRuntime().exec("java -cp /home/tiago/IdeaProjects/SD/out/production/SD/:/home/tiago/IdeaProjects/SD/lib/amqp-client-5.11.0.jar:/home/tiago/IdeaProjects/SD/lib/slf4j-api-1.7.30.jar:/home/tiago/IdeaProjects/SD/lib/slf4j-simple-1.7.30.jar edu.ufp.inf.sd.project.consumer.Consumer2 localhost 5672 project_logs_exchange " + w.getId() + "_results");
                } catch (IOException e ) {
                    e.printStackTrace();
                }
            }
        }
        for(Process p : processes) { //destruir os processos
            p.destroy();
        }
     }

    /**
     * lê de ficheiro
     * @param idJ idjobgroup
     */
     private void readFromFile(int idJ) {
         HashMap<String,String> makespans = new HashMap<>(); //key - id do worker
         try (BufferedReader br = new BufferedReader(new FileReader("/home/tiago/IdeaProjects/SD/src/edu/ufp/inf/sd/project/logs/makespanWorker.txt"))) {
             String line;
             String []s;
             while ((line = br.readLine()) != null) {
                 s = line.split(" ");
                 String[] id = s[2].split("'|\\_");
                 if(!s[3].contains("Stopping")) {
                     if (s[5] != null && !s[4].contains("Strategy")) {
                         //System.out.println(s[5]);
                         makespans.put(id[1],s[5]);
                     }
                 }
             }
             System.out.println(makespans);
             JobGroupRI j = this.getJobGroupId(idJ);
             assert j!= null;
             //for(String workerid : makespans.keySet()) {
                // j.updateRMQ(Integer.parseInt(workerid),Integer.parseInt(makespans.get(workerid)));
             //}
         } catch(IOException e){
             e.printStackTrace();
         }
     }

    /**
     * calcula se é possivel associar outro worker ao jobgroup
     * @param idJobGroup id jobgroup para associar worker
     * @return retorna o maximo de cŕeditos
     */
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
