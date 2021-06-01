package edu.ufp.inf.sd.project.consumer;


import com.rabbitmq.client.*;
import edu.ufp.inf.sd.project.util.RabbitUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class Consumer2 { //Não está a ser utilizado
    /**
     * Seria o consumidor para o Jobgroup mas não está a ser utilizada, pois jobpgroup possui metodo consumidor
     * @param argv
     */
    public static void main(String[] argv) {
        String host=argv[0];
        int port=Integer.parseInt(argv[1]);
        String exchangeName=argv[2];
        HashMap<String, String> maskespans = new HashMap<>();
        try {
            Connection connection = RabbitUtils.newConnection2Server(host, port, "guest", "guest");
            Channel channel = RabbitUtils.createChannel2Server(connection);
            channel.exchangeDeclare(exchangeName, BuiltinExchangeType.TOPIC);

            //System.out.println("main(): argv.length=" + argv.length);

            if (argv.length < 4) {
                System.err.println("Usage: ReceiveLogsTopic [HOST] [PORT] [EXCHANGE] [BindingKey1] [RoutingKey2]");
                System.exit(1);
            }
            String queueName = argv[3];

            //Bind to each routing key (received from args[3] upward)


            System.out.println(" [*] Waiting for messages... to exit press CTRL+C");
            //Create callback that will receive messages from topic
            DeliverCallback deliverCallback=(consumerTag, delivery) -> {
                BufferedWriter writer = new BufferedWriter(new FileWriter("/home/tiago/IdeaProjects/SD/src/edu/ufp/inf/sd/project/logs/makespanWorker.txt",true));
                String message=new String(delivery.getBody(), StandardCharsets.UTF_8);
                System.out.println(" [x] Received '" +
                        delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");

                maskespans.put(delivery.getEnvelope().getRoutingKey(),message);
                writer.write("[x] Received: '" + delivery.getEnvelope().getRoutingKey() + "': " + message + "\n");
                writer.close();
            };

            CancelCallback cancelCallback=(consumerTag) -> {
                System.out.println(" [x] Cancel callback activated: " + consumerTag);
            };

            channel.basicConsume(queueName, true, deliverCallback, cancelCallback);
            //Current Thread waits till interrupted (avoids finishing try-with-resources which closes channel)
            //Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e);
        }
    }
}
