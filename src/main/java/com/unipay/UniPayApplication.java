package com.unipay;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;


@EnableJpaAuditing
@SpringBootApplication
@EnableAspectJAutoProxy
public class UniPayApplication {

   public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        dotenv.entries().forEach(entry ->
                System.setProperty(entry.getKey(), entry.getValue())
        );

        SpringApplication.run(UniPayApplication.class, args);
    }
    /* public static void main(String[] args) {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            String ipAddress = localHost.getHostAddress();
            String hostName = localHost.getHostName();

            System.out.println("Host Name: " + hostName);
            System.out.println("IP Address: " + ipAddress);
        } catch (UnknownHostException e) {
            System.err.println("Unable to determine IP address.");
            e.printStackTrace();
        }
    }*/
}
