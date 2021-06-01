package edu.ufp.inf.sd.project.server;

import com.rabbitmq.client.*;
import edu.ufp.inf.sd.project.client.WorkerRI;
import edu.ufp.inf.sd.project.util.RabbitUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JobGroupImpl implements JobGroupRI {
    private int id;
    private int credits;
    private String filename; //só utilizado para RMQ
    private int algorithm;
    private User owner;
    private ArrayList<WorkerRI> workers = new ArrayList<>();
    private HashMap<Integer, Integer> workerMakespan = new HashMap<>();
    private SchedulingState schedulingState;
    private HashMap<String,String> workerMakespanHelper = new HashMap<>();
    private int countStopMessages;
    public JobGroupImpl(User owner, int algorithm, int credits, String filename, int id) {
        this.owner = owner;
        //schedulingState = true;
        this.id = id;
        this.algorithm = algorithm;
        this.credits = credits;
        this.filename = filename;
        this.countStopMessages = 0;
    }

    /**
     * Adiciona um worker ao jobgroup
     * @param worker worker a adicionar
     */
    @Override
    public void addWorker(WorkerRI worker) {
        if(!this.workers.contains(worker)) {
            this.workers.add(worker);
        }
    }

    public void remWorker(WorkerRI workerRI) {
        this.workers.remove(workerRI);
    }

    @Override
    public String toString() {
        return "JobGroup{" +
                "id= " + id +
                "owner=" + owner +
                ", credits=" + credits +
                ", filename='" + filename + '\'' +
                ", algorithm=" + algorithm +
                ", workers= " + workers +
                //", schedulingState=" + schedulingState +
                '}';
    }
    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    @Override
    public String getFilename() throws RemoteException {
        return filename;
    }

    public ArrayList<WorkerRI> getWorkers() {
        return workers;
    }

    @Override
    public SchedulingState getSchedulingState() {
        return schedulingState;
    }

    @Override
    public void setSchedulingState(SchedulingState schedulingState) throws RemoteException {
        this.schedulingState = schedulingState;
        notifyWorkers();
    }

    public void notifyWorkers() throws RemoteException {
        for(WorkerRI w : workers) {
            w.update();
        }
    }

    /**
     * vai buscar o makespan ao state e adiciona ao hasmap de worker-hashmap e distribui os créditos
     * @param workerID id worker adicionar ao hashmap e adicionar makespan ao hasmap
     * @throws RemoteException
     */
    @Override
    public void update(int workerID) throws RemoteException {
            int maskespan = getWorker(workerID).getLastWorkerState().getMakespan();
            this.workerMakespan.put(workerID,maskespan);
            if(this.workerMakespan.size() == this.workers.size()) { //calcular o melhor makespan porque já acabou
                int idWorkerMinMake = calculateMinMakespan();
                this.workerMakespan.clear(); //limpar hashmap de makespans :)
                for(WorkerRI w : this.workers) {
                        if (w.getId() == idWorkerMinMake) {
                            w.addCredits(11);
                            this.credits -= 11;
                        } else {
                            w.addCredits(1);
                            this.credits--;
                        }
                    }
            }
    }

    /**
     * faz o update dos créditios dos workers mas para o RMQ
     * @throws RemoteException
     */
    @Override
    public void updateRMQ() throws RemoteException {
        if(this.workerMakespan.size() == this.workers.size()) {
            int idWorkerMinMake = calculateMinMakespan();
            this.workerMakespan.clear();
            for(WorkerRI w : this.workers) {
                if(w.getId() == idWorkerMinMake) {
                    w.addCredits(11);
                    this.credits -=11;
                } else {
                    w.addCredits(1);
                    this.credits--;

                }
            }
        }
    }

    /**
     * vai buscar worker pelo id
     * @param workerID id worker
     * @return retorna o worker ou null se nao encontrar
     */
    public WorkerRI getWorker(int workerID) {
        for(WorkerRI w : this.workers) {
            if(w.getId() == workerID) {
                return w;
            }
        }
        return null;
    }

    /**
     * calcula o minmakespan no hashmap a quem deve atribuir os créditos
     * @return return o ID do worker que vai receber os 11 creditos
     */
    public int calculateMinMakespan() {
        int aux = Integer.MAX_VALUE;
        int wID = 0;
        for(int workerId : this.workerMakespan.keySet()) {
            if (workerMakespan.get(workerId) < aux) {
                aux = workerMakespan.get(workerId);
                wID = workerId;
            }
        }
        return wID;
    }

    /**
     * metodo consumidor do jobggroup, adiciona os min makespans dos workers ao hasmap e chama o metodo para distribuir os créditos
     * @param argv recebe a porta o host e o exchange name e o nome da fila para ir buscar makespans
     * @throws RemoteException
     */
    @Override
    public void waitforWorker(String []argv) throws RemoteException {
        String host=argv[0];
        int port=Integer.parseInt(argv[1]);
        String exchangeName=argv[2];
        try {
            Connection connection = RabbitUtils.newConnection2Server(host, port, "guest", "guest");
            Channel channel = RabbitUtils.createChannel2Server(connection);
            channel.exchangeDeclare(exchangeName, BuiltinExchangeType.TOPIC);

            if (argv.length < 4) {
                System.err.println("Usage: ReceiveLogsTopic [HOST] [PORT] [EXCHANGE] [NomeFila] ");
                System.exit(1);
            }
            String queueName = argv[3];

            System.out.println(" [*] Waiting for messages... to exit press CTRL+C");
            //Create callback that will receive messages from topic
            String pathaux = System.getProperty("user.dir");
            String path = pathaux.replace("out/production/SD","src/edu/ufp/inf/sd/project/logs/");

            DeliverCallback deliverCallback=(consumerTag, delivery) -> {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(path + "makespanWorker.txt", true));
                    String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                    System.out.println(" [x] Received '" + delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");

                    writer.write("[x] Received: '" + delivery.getEnvelope().getRoutingKey() + "': " + message + "\n");
                    writer.close();
                    if(!message.contains("Stopping") && !message.contains("Strategy")) {
                        workerMakespanHelper.put(delivery.getEnvelope().getRoutingKey(), message);
                    }
                if(message.contains("Stopping")) {
                        this.countStopMessages++;
                        if(this.countStopMessages == this.workers.size()) {
                            this.countStopMessages = 0;
                            for (String s : workerMakespanHelper.keySet()) {
                                String[] h = s.split("_");
                                String[] x = workerMakespanHelper.get(s).split("= ");
                                //Logger.getLogger(this.getClass().getName()).log(Level.INFO,h[0] +  " " + x[1]);
                                this.workerMakespan.put(Integer.parseInt(h[0]), Integer.parseInt(x[1]));
                                if (this.workerMakespan.size() == this.workers.size()) {
                                    this.updateRMQ();
                                    Logger.getLogger(this.getClass().getName()).log(Level.INFO,JobShopServer.printWorkers(1));
                                    //Logger.getLogger(this.getClass().getName()).log(Level.INFO,workerMakespanHelper.toString());
                                }
                            }
                        }
                    }
            };

            CancelCallback cancelCallback=(consumerTag) -> {
                System.out.println(" [x] Cancel callback activated: " + consumerTag);
            };

            channel.basicConsume(queueName, true, deliverCallback, cancelCallback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
