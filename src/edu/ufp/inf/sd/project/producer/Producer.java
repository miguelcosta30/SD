package edu.ufp.inf.sd.project.producer;

import com.rabbitmq.client.*;
import edu.ufp.inf.sd.project.util.RabbitUtils;
import edu.ufp.inf.sd.project.util.geneticalgorithm.CrossoverStrategies;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

/**
 * RabbitMQ speaks multiple protocols. This tutorial uses AMQP 0-9-1, which is
 * an open, general-purpose protocol for messaging. There are a number of
 * clients for RabbitMQ in many different languages. We'll use the Java client
 * provided by RabbitMQ.
 * <p>
 * Download client library (amqp-client-4.0.2.jar) and its dependencies (SLF4J
 * API and SLF4J Simple) and copy them into lib directory.
 * <p>
 * Jargon terms:
 * RabbitMQ is a message broker, i.e., a server that accepts and forwards messages.
 * Producer is a program that sends messages (Producing means sending).
 * Queue is a post box which lives inside a RabbitMQ broker (large message buffer).
 * Consumer is a program that waits to receive messages (Consuming means receiving).
 * The server, client and broker do not have to reside on the same host
 *
 * @author rui
 */
public class Producer {
    /**
     * recebe por argumento o ID do worker (que vai ser nome da fila) e ficheiro JSSP para enviar ao worker
     * @param argv
     * @throws IOException
     */
    public static void main(String[] argv) throws IOException {
        //Connection connection=null;
        //Channel channel=null;

        if(argv.length < 4) {
            System.err.println("Usage: ReceiveLogsTopic [HOST] [PORT] [EXCHANGE] [WorkerID] [Ficheiro]");
            System.exit(0);
        }

        String host = argv[0];
        int port = Integer.parseInt(argv[1]);
        String exchangeName = argv[2];
        String workerid = argv[3];

        String filename = RabbitUtils.getMessage(argv,4); //ir buscar o caminho do ficheiro
        String pathaux = System.getProperty("user.dir");
        String path = pathaux.replace("out/production/SD","src/edu/ufp/inf/sd/project/logs/");

        BufferedWriter writer = new BufferedWriter(new FileWriter(path + "workersInstanceAndID.txt",true));
        writer.write("[" + workerid + "]" + " Filename = " + filename + "\n" );
        writer.close();

        try (Connection connection=RabbitUtils.newConnection2Server(host, port, "guest", "guest");
             Channel channel = RabbitUtils.createChannel2Server(connection)) {

            System.out.println("[!] Worker " + workerid + "running instance " + filename + " ...");

            channel.exchangeDeclare(exchangeName, BuiltinExchangeType.TOPIC);
                //Messages are not persisted (will be lost if no queue is bound to exchange yet)
            AMQP.BasicProperties props=null;//=MessageProperties.PERSISTENT_TEXT_PLAIN

            sendMessage(channel,filename,workerid,exchangeName);

            //Estratégia 2
            sendMessage(channel, String.valueOf(CrossoverStrategies.TWO.strategy),workerid,"");
            Thread.sleep(2000);

            //Estratégia 3
            sendMessage(channel, String.valueOf(CrossoverStrategies.THREE.strategy),workerid,"");
            Thread.sleep(2000);

            sendMessage(channel,"stop",workerid,"");

        } catch (IOException | TimeoutException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    public static void sendMessage(Channel channel, String message,String queuename, String exchangeName) throws IOException {
        channel.basicPublish(exchangeName,queuename, null, message.getBytes(StandardCharsets.UTF_8));
        System.out.println(" [x] Sent '" + message + "'");
    }

}
