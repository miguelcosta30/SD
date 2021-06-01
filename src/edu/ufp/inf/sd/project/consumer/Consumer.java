package edu.ufp.inf.sd.project.consumer;

/**
 * Consumer will keep running to listen for messages from queue and print them out.
 * 
 * DefaultConsumer is a class implementing the Consumer interface, used to buffer 
 * the messages pushed to us by the server.
 * 
 * Compile with RabbitMQ java client on the classpath:
 *  javac -cp amqp-client-4.0.2.jar RPCServer.java RPCClient.java
 * 
 * Run with need rabbitmq-client.jar and its dependencies on the classpath.
 *  java -cp .:amqp-client-4.0.2.jar:slf4j-api-1.7.21.jar:slf4j-simple-1.7.22.jar Recv
 *  java -cp .:amqp-client-4.0.2.jar:slf4j-api-1.7.21.jar:slf4j-simple-1.7.22.jar Producer
 * 
 * OR
 * export CP=.:amqp-client-4.0.2.jar:slf4j-api-1.7.21.jar:slf4j-simple-1.7.22.jar
 * java -cp $CP Producer
 * java -cp %CP% Producer
 * 
 * The client will print the message it gets from the publisher via RabbitMQ.
 * The client will keep running, waiting for messages (Use Ctrl-C to stop it).
 * Try running the publisher from another terminal.
 *
 * Check RabbitMQ Broker runtime info (credentials: guest/guest4rabbitmq):
 *  http://localhost:15672/
 * 
 * 
 * @author rui
 */

import com.rabbitmq.client.*;
import edu.ufp.inf.sd.project.util.RabbitUtils;
import edu.ufp.inf.sd.project.util.geneticalgorithm.CrossoverStrategies;
import edu.ufp.inf.sd.project.util.geneticalgorithm.GeneticAlgorithmJSSP;

import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;

public class Consumer {
    public static String message;

    /**
     * recebe pela fila o ficheiro JSSP para correr o algoritmo Genetic Alg.
     * @param argv
     * @throws Exception
     */
    public static void main(String[] argv) throws Exception {
            //Read args passed via shell command
            String host=argv[0];
            int port=Integer.parseInt(argv[1]);
            String exchangeName=argv[2];

            //DO NOT USE try-with-resources HERE because closing resources (channel) will prevent receiving any messages.
            try {

                Connection connection = RabbitUtils.newConnection2Server(host, port, "guest", "guest");
                Channel channel = RabbitUtils.createChannel2Server(connection);

                channel.exchangeDeclare(exchangeName, BuiltinExchangeType.TOPIC);


                String queueName = channel.queueDeclare().getQueue();


                System.out.println("main(): argv.length=" + argv.length);

                if (argv.length < 4) {
                    System.err.println("Usage: ReceiveLogsTopic [HOST] [PORT] [EXCHANGE] [BindingKey1] [RoutingKey2]");
                    System.exit(1);
                }

                //Bind to each routing key (received from args[3] upward)
                for (int i=3; i < argv.length; i++) {
                    String bindingKey = argv[i];
                    System.err.println("main(): add queue bind to queue = " + queueName + ", with bindingKey = " + bindingKey);
                    channel.queueBind(queueName, exchangeName, bindingKey);
                }

                System.out.println(" [*] Waiting for messages... to exit press CTRL+C");

                DeliverCallback deliverCallback=(consumerTag, delivery) -> {
                    String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                    System.out.println(" [x] Received '" + delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");

                    runGA(message,delivery.getEnvelope().getRoutingKey());
                };

                CancelCallback cancelCallback=(consumerTag) -> {
                    System.out.println(" [x] Cancel callback activated: " + consumerTag);
                };

                channel.basicConsume(queueName, true, deliverCallback, cancelCallback);


            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    public static void runGA(String jsspInstance, String queue) throws RemoteException {
        String resultsQueue = queue + "_results";
        CrossoverStrategies strategy = CrossoverStrategies.ONE;
        GeneticAlgorithmJSSP ga = new GeneticAlgorithmJSSP(jsspInstance, queue, strategy);
        ga.run();
    }

}


