package edu.ufp.inf.sd.project.server;

import edu.ufp.inf.sd.project.client.WorkerRI;
import edu.ufp.inf.sd.rmi.util.rmisetup.SetupContextRMI;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Properties;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>
 * Title: Projecto SD</p>
 * <p>
 * Description: Projecto apoio aulas SD</p>
 * <p>
 * Copyright: Copyright (c) 2017</p>
 * <p>
 * Company: UFP </p>
 *
 * @author Rui S. Moreira
 * @version 3.0
 */
public class JobShopServer {

    /**
     * Context for running a RMI Servant on a SMTP_HOST_ADDR
     */
    private SetupContextRMI contextRMI;
    /**
     * Remote interface that will hold reference MAIL_TO_ADDR the Servant impl
     */
    private JobShopFactoryRI jobShopFactoryRI;
    private static int workerID = 0;

    public static int count() {
        workerID++;
        return workerID;
    }

    private static ArrayList<JobGroupImpl> jobGroups = new ArrayList<>();

    public static void main(String[] args) {
        if (args != null && args.length < 3) {
            System.err.println("usage: java [options] edu.ufp.sd._01_helloworld.server.HelloWorldServer <rmi_registry_ip> <rmi_registry_port> <service_name>");
            System.exit(-1);
        } else {
            //1. ============ Create Servant ============
            JobShopServer hws = new JobShopServer(args);
            //2. ============ Rebind servant on rmiregistry ============
            hws.rebindService();
        }
        /*
        try {
            loadProperties();
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
    }

    /**
     * 
     * @param args 
     */
    public JobShopServer(String args[]) {
        try {
            //============ List and Set args ============
            SetupContextRMI.printArgs(this.getClass().getName(), args);
            String registryIP = args[0];
            String registryPort = args[1];
            String serviceName = args[2];
            //============ Create a context for RMI setup ============
            contextRMI = new SetupContextRMI(this.getClass(), registryIP, registryPort, new String[]{serviceName});
        } catch (RemoteException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
        }
    }

    private void rebindService() {
        try {
            //Get proxy MAIL_TO_ADDR rmiregistry
            Registry registry = contextRMI.getRegistry();
            //Bind service on rmiregistry and wait for calls
            if (registry != null) {
                //============ Create Servant ============
                jobShopFactoryRI = new JobShopFactoryImpl();

                //Get service url (including servicename)
                String serviceUrl = contextRMI.getServicesUrl(0);
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "going MAIL_TO_ADDR rebind service @ {0}", serviceUrl);

                //============ Rebind servant ============
                //Naming.bind(serviceUrl, helloWorldRI);
                registry.rebind(serviceUrl, jobShopFactoryRI);
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "service bound and running. :)");
            } else {
                //System.out.println("HelloWorldServer - Constructor(): create registry on port 1099");
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "registry not bound (check IPs). :(");
                //registry = LocateRegistry.createRegistry(1099);
            }
        } catch (RemoteException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void loadProperties() throws IOException {

        Logger.getLogger(Thread.currentThread().getName()).log(Level.INFO, "goig MAIL_TO_ADDR load props...");
        // create and load default properties
        Properties defaultProps = new Properties();
        FileInputStream in = new FileInputStream("defaultproperties.txt");
        defaultProps.load(in);
        in.close();

        BiConsumer<Object, Object> bc = (key, value) ->{
            System.out.println(key.toString()+"="+value.toString());
        };
        defaultProps.forEach(bc);

        // create application properties with default
        Properties props = new Properties(defaultProps);

        FileOutputStream out = new FileOutputStream("defaultproperties2.txt");
        props.store(out, "---No Comment---");
        out.close();
    }

    /**
     * adiciona jobgroup ao array estatico de jobgroups
     * @param j jobgroup a adicoonar
     */
    public static void addJobGroup(JobGroupImpl j) {
            jobGroups.add(j);
    }

    /**
     * verfica se arraylist contem jobgroup
     * @param id id do jobgroup para ver se existe
     * @return true - existe | false - nao existe
     */
    public static boolean containsJobGroup(int id) {
        for(JobGroupImpl j : jobGroups) {
            if(j.getId() == id) {
                return true;
            }
        }
        return false;
    }

    /**
     * verficia se contem worker
     * @param id id worker para ver se existe
     * @return true - existe | false - nao existe
     */
    public static boolean containsWorker(int id) {
        for(JobGroupImpl j : jobGroups) {
            for(WorkerRI w : j.getWorkers()) {
                if(w.getId() == id) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * apaga um jobgroup
     * @param id id do jobgroup para apgar
     * @return true - bem sucedido | false - mal sucedido
     */
    public static boolean deleteJobGroup(int id) {
        JobGroupImpl j = getJobGroup(id);
        if(j != null) {
            jobGroups.remove(j);
            return true;
        }
        return false;
    }

    /**
     * verficar se o JSS já está a ser usado em algum JobGroup
     * @param jss intacia a verificar
     * @return true - utilizado | false - nao utilizado
     * @throws RemoteException
     */
    public static boolean containsJSS(String jss) throws RemoteException{
        for(JobGroupImpl j : jobGroups) {
            if(j.getFilename().compareTo(jss) == 0) {
                return true; //instancia de JSS a ser utilizada
            }
        }
        return false;
    }

    /**
     * mostra os workers existestes num jobgroup
     * @param id id jobgroup
     * @return string de workers
     */
    public static String printWorkers(int id) {
       String s = "";
       for(JobGroupImpl w: jobGroups) {
           if (w.getId() == id) {
               if (w.getWorkers() != null) {
                   s = s + w.getWorkers().toString();
               }
           }
       }
       return s;
    }

    /**
     * procura jobgroup no arraylist
     * @param id id jobgroup
     * @return retorna jobgroup ou null se nao encontrar
     */
    public static JobGroupImpl getJobGroup(int id) {
        for(JobGroupImpl j : jobGroups) {
            if(j.getId() == id ) {
                return j;
            }
        }
        return null;
    }

}
