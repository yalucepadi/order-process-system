package com.hacom.order_process_system.service;

import com.cloudhopper.smpp.SmppBindType;
import com.cloudhopper.smpp.SmppSession;

import com.cloudhopper.smpp.SmppSessionConfiguration;
import com.cloudhopper.smpp.impl.DefaultSmppClient;
import com.cloudhopper.smpp.impl.DefaultSmppSessionHandler;
import com.cloudhopper.smpp.pdu.PduRequest;
import com.cloudhopper.smpp.pdu.PduResponse;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.cloudhopper.smpp.type.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Service
public class SmsService {

    private static final Logger logger = LoggerFactory.getLogger(SmsService.class);

    private DefaultSmppClient client;
    private SmppSession session;

    @PostConstruct
    public void init() {
        try {
            logger.info("Initializing SMPP client");
            client = new DefaultSmppClient();

            // Configuración SMPP (ejemplo - ajustar según el proveedor)
            SmppSessionConfiguration config = new SmppSessionConfiguration();
            config.setWindowSize(1);
            config.setName("HacomApp");
            config.setType(SmppBindType.TRANSCEIVER);

            config.setHost("localhost"); // Cambiar por la IP del servidor SMPP
            config.setPort(2775); // Puerto SMPP
            config.setSystemId("test"); // Sistema ID
            config.setPassword("test"); // Password
            config.getLoggingOptions().setLogBytes(true);

            // Crear sesión
            session = client.bind(config, new DefaultSmppSessionHandler() {
                @Override
                public void firePduRequestExpired(PduRequest pduRequest) {
                    logger.warn("PDU request expired: {}", pduRequest);
                }
            });

            logger.info("SMPP session established successfully");

        } catch (Exception e) {
            logger.error("Failed to initialize SMPP client: {}", e.getMessage());
            // En un entorno real, podrías querer que la aplicación no falle por esto
            // o implementar reconexión automática
        }
    }

    public void sendSms(String phoneNumber, String message) {
        if (session == null || !session.isBound()) {
            logger.warn("SMPP session not available. SMS not sent to {}: {}", phoneNumber, message);
            return;
        }

        try {
            logger.info("Sending SMS to {}: {}", phoneNumber, message);

            SubmitSm submit = new SubmitSm();
            submit.setSourceAddress(new Address((byte) 0x00, (byte) 0x00, "12345"));
            submit.setDestAddress(new Address((byte) 0x01, (byte) 0x01, phoneNumber));
            submit.setShortMessage(message.getBytes());

            PduResponse response = session.submit(submit, 10000);

            if (response.getCommandStatus() == 0) {
                logger.info("SMS sent successfully to {}", phoneNumber);
            } else {
                logger.error("Failed to send SMS to {}. Status: {}", phoneNumber, response.getCommandStatus());
            }

        } catch (Exception e) {
            logger.error("Error sending SMS to {}: {}", phoneNumber, e.getMessage());
        }
    }

    @PreDestroy
    public void destroy() {
        try {
            if (session != null) {
                session.close();
                logger.info("SMPP session closed");
            }
            if (client != null) {
                client.destroy();
                logger.info("SMPP client destroyed");
            }
        } catch (Exception e) {
            logger.error("Error closing SMPP resources: {}", e.getMessage());
        }
    }
}