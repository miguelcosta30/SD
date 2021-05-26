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


public class Consumer {

    public static void main(String[] argv) throws Exception {

            //Read args passed via shell command
            String host=argv[0];
            int port=Integer.parseInt(argv[1]);
            String exchangeName=argv[2];

            //DO NOT USE try-with-resources HERE because closing resources (channel) will prevent receiving any messages.
            try {

                // TODO: Create a channel to RabbitMQ
                Connection connection = RabbitUtils.newConnection2Server(host, port, "guest", "guest");
                Channel channel = RabbitUtils.createChannel2Server(connection);

                // TODO: Declare exchange of type TOPIC
                channel.exchangeDeclare(exchangeName, BuiltinExchangeType.TOPIC);

                // TODO: Create a non-durable, exclusive, autodelete queue with a generated name
                String queueName = channel.queueDeclare().getQueue();;


                System.out.println("main(): argv.length=" + argv.length);

                if (argv.length < 4) {
                    System.err.println("Usage: ReceiveLogsTopic [HOST] [PORT] [EXCHANGE] [BindingKey1] [RoutingKey2]");
                    System.exit(1);
                }

                //Bind to each routing key (received from args[3] upward)
                for (int i=3; i < argv.length; i++) {
                    String bindingKey = argv[i];
                    System.err.println("main(): add queue bind to queue = " + queueName + ", with bindingKey = " + bindingKey);

                    // TODO: Create binding: tell exchange to send messages to a queue
                    channel.queueBind(queueName, exchangeName, bindingKey);

                }

                System.out.println(" [*] Waiting for messages... to exit press CTRL+C");

                //Create callback that will receive messages from topic
                DeliverCallback deliverCallback=(consumerTag, delivery) -> {
                    String message=new String(delivery.getBody(), "UTF-8");
                    System.out.println(" [x] Received '" +
                            delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
                };
                CancelCallback cancelCallback=(consumerTag) -> {
                    System.out.println(" [x] Cancel callback activated: " + consumerTag);
                };

                // TODO: Consume with deliver and cancel callbacks
                channel.basicConsume(queueName, true, deliverCallback, cancelCallback);

                //Current Thread waits till interrupted (avoids finishing try-with-resources which closes channel)
                //Thread.currentThread().join();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
}
