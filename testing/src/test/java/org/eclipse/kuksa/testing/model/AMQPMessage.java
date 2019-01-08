package org.eclipse.kuksa.testing.model;

import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.proton.*;

import org.eclipse.hono.client.CommandClient;
import org.eclipse.hono.client.HonoClient;
import org.eclipse.hono.util.BufferResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import static org.junit.Assert.assertEquals;

@Component
public class AMQPMessage {

    @Autowired
    private Environment env;
    private Vertx vertx = Vertx.vertx(new VertxOptions().setWorkerPoolSize(10));
    ProtonClient protonClient = ProtonClient.create(vertx);
    private String address = "control/ASSYSTEM_TENANT4/assystem1";

    @Autowired
    private HonoConnector honoConnector;

    /*
        public Message toAmqp() {

            Message message = ProtonHelper.message();

            message.setSubject(AMQP_SUBJECT);

            Map<Symbol, Object> map = new HashMap<>();
            map.put(Symbol.valueOf(AMQP_RETAIN_ANNOTATION), this.isRetain);
            map.put(Symbol.valueOf(AMQP_QOS_ANNOTATION), this.qos.value());
            MessageAnnotations messageAnnotations = new MessageAnnotations(map);
            message.setMessageAnnotations(messageAnnotations);

            message.setAddress(this.topic);

            Header header = new Header();
            header.setDurable(this.qos != MqttQoS.AT_MOST_ONCE);
            message.setHeader(header);

            // the payload could be null (or empty)
            if (this.payload != null)
                message.setBody(new Data(new Binary(this.payload.getBytes())));

            return message;
        }
    */
/*
    public void sendMessage(String messageBody, long timeout, TimeUnit timeUnit) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        protonClient.connect(env.getProperty("HONO_DISPATCH_ROUTER_STABLE"), 5671, event -> {
            ProtonConnection connection = event.result().open();

            Target target = new Target();
            target.setAddress(address);
            target.setCapabilities(Symbol.getSymbol("topic"));

            ProtonSender sender = connection.createSender(address);
            sender.setTarget(target);
            sender.open();



            Message message = Message.Factory.create();
            message.setBody(new AmqpValue(messageBody));
            message.setSubject("light");
            message.setTtl(5);
            message.setReplyTo(address);


            // COMMANDTYPE - msg-subject
            // COMMANDVALUE - msg-content
            // COMMAND ID - msg-id
            // REPLAY TO ADDRESS - msg-reply-to
            // LOGGINGFORMAT - log-msg
            // NUMBER OF MESSAGES - count


            message.setAddress(address);
            sender.send(message, delivery -> latch.countDown());
        });
        latch.await(timeout, timeUnit);

        vertx.close();
    }
*/
    public AMQPMessage() {
    System.out.println("Creating new AMQP MESSAGE");
    // Create HonoClient and connect to the given Hono instance
    Future<HonoClient> clientFuture = honoConnector.connectToHonoMessaging();

    // Create CommandClient for sending Command to a specific device
		clientFuture.map(client -> {
            client.getOrCreateCommandClient("TENANT5", "4712").map(commandClient -> {

                calculateSomethingAndSendCommand(commandClient);
                return commandClient;
            });

            return client;

		});
    }

    Future<BufferResult> result;

    public void calculateSomethingAndSendCommand(CommandClient commandClient) {
		/*QueryResult result = influxDB.getTelemetryData();
		for (Result res : result.getResults()) {
			System.out.println("Result: " + res.getSeries());
		}*/
		System.out.println("Will send command");
        Command cmd = new Command("Backward", "{\"mode\":0,\"command\":\"E\",\"speed\":\"360\"}");
        result = commandClient.sendCommand(cmd.getCommand(), Buffer.buffer(cmd.getPayload()));

    }

    public Future<BufferResult> getBufferResult() {
        return result;
    }
}
