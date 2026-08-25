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
                .post("order")
                .type(Order.class)
                .to("direct:orderManagment");

        from("direct:orderManagment")
                .routeId("processOrderID")

                .setProperty("correlationId")
                .simple("${exchangeId}")

                .setProperty("originalOrder")
                .body()

                .log("CorrelationId=${exchangeProperty.correlationId} - Order recibido")
                .log("Body antes de obtener token = ${body}")

                .to("direct:getToken")

                .setProperty("authToken")
                .simple("${body.accessToken}")

                .log("CorrelationId=${exchangeProperty.correlationId} - Token listo para usar: ${exchangeProperty.authToken}")

                // Se restaura la orden original en el body. El envío real a ProcessOrden
                // (con el header Authorization y su futura conversión a SOAP/WSDL) se
                // implementa en el siguiente paso.
                .setBody(exchangeProperty("originalOrder"))

                .log("CorrelationId=${exchangeProperty.correlationId} - Orden lista para enviar a ProcessOrden (pendiente de implementar)");


    }
}
