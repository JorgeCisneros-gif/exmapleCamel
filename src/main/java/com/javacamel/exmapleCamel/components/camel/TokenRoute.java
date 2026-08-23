package com.javacamel.exmapleCamel.components.camel;

import com.javacamel.exmapleCamel.beans.TokenAPI;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TokenRoute  extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        from("direct:getToken")
                .routeId("getTokenID")
                .log("CorrelationId=${exchangeProperty.correlationId} - Consultando Redis")
                .to("direct:redisGetToken")
                .log("Resultado Redis: ${body}");
                /*
                .log("CorrelationId=${exchangeProperty.correlationId} - Solicitando token")
                .setBody(constant("""
                    {
                        "username": "camel",
                        "password": "123456"
                    }
                """))
                .setHeader("Content-Type", constant("application/json"))
                .removeHeader(Exchange.HTTP_PATH)
                .removeHeader(Exchange.HTTP_URI)
                .to("http://localhost:3030/api/getToken")
                .unmarshal().json(TokenAPI.class)
                .log("Token recibido: ${body.accessToken}");
                */

    }
}
