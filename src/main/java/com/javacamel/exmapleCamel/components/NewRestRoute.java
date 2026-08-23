package com.javacamel.exmapleCamel.components;

import com.javacamel.exmapleCamel.beans.InboundRestProcessingbean;
import com.javacamel.exmapleCamel.beans.NameAddress;
import com.javacamel.exmapleCamel.procesor.InboundMessageProcessor;
import jakarta.jms.JMSException;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Predicate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.stereotype.Component;

import java.net.ConnectException;

@Component
public class NewRestRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        Predicate isCityAjax = header("userCity").isEqualTo("AJAX");

        onException(JMSException.class, ConnectException.class)
                .routeId("jmsExceptionRouteId")
                .handled(true)
                    .log(LoggingLevel.INFO, "JMSException has occurred; handling gracefully");


        restConfiguration()
                .component("jetty")
                .host("0.0.0.0")
                .port(8084)
                .bindingMode(RestBindingMode.json)
                .enableCORS(true);



        rest("testcamel")
                .produces("application/json")
                .post("nameAddress").type(NameAddress.class)
                .to("direct:managementNameAddress");

        /*
        from("direct:nameAddress")
                .routeId("newRestRouteID")
                .log(LoggingLevel.INFO, "${body}")
                .process(new InboundMessageProcessor())
                .convertBodyTo(String.class)
                .to("file:src/data/output?fileName=outputFile.csv&fileExist=append&appendChars=\\n");

         */
        from("direct:managementNameAddress")
                .routeId("managementNameAddressId")
                .bean(new InboundRestProcessingbean())
                //Setup Rule
                //if city AJAX -send ACtimeMg
                //else sent to bd and activemq
                .choice()
                .when(isCityAjax)
                    .to("direct:toActiveMQ")
                    .to("direct:redisTest")
                .otherwise()
                    .to("direct:toDB")
                    .to("direct:toActiveMQ")

                .end()

                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200))
                    .transform().simple("Message Process an Result Generated with Body : ${body}");



        from("direct:toDB")
                .routeId("toDBId")
                .log(LoggingLevel.INFO, ">>>>> in DB EP")
                .to("jpa:" + NameAddress.class.getName());


        from("direct:toActiveMQ")
                .routeId("toActiveMQId")
                .log(LoggingLevel.INFO, ">>>>> in ActiveMQ EP")
                .to("activemq6:queue:nameaddressqueue?exchangePattern=InOnly");



    }
}