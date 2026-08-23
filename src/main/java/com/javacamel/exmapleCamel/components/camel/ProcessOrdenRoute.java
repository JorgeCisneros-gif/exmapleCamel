package com.javacamel.exmapleCamel.components.camel;


import com.javacamel.exmapleCamel.beans.Order;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.stereotype.Component;


@Component
public class ProcessOrdenRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {


        restConfiguration()
                .component("jetty")
                .host("0.0.0.0")
                .port(8084)
                .bindingMode(RestBindingMode.json)
                .enableCORS(true);

        rest("/api/ProcesOrder")
                .produces("application/json")
                .post("Order")
                .type(Order.class)
                .to("direct:orderManagment");

        from("direct:orderManagment")
                .routeId("processOrderID")

                .setProperty("correlationId")
                .simple("${exchangeId}")

                .setProperty("originalOrder")
                .body()

                .log("CorrelationId=${exchangeProperty.correlationId} - Order recibido")
                .log("Body antes de Redis = ${body}")

                .setBody(constant(null))

                .log("Body antes de testRedis = ${body}")

                .to("direct:testRedis");


    }
}
