package com.javacamel.exmapleCamel.components.camel;

import com.javacamel.exmapleCamel.beans.TokenAPI;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class TokenRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {

        // Punto de entrada: deja un TokenAPI válido en el body.
        // 1) Consulta Redis (RedisRoute -> direct:redisGetToken).
        // 2) Si no hay token vigente (null/vacío, incluye el caso "expiró") -> lo solicita y lo guarda.
        // 3) Si hay token vigente en Redis -> lo deserializa y lo entrega tal cual.
        from("direct:getToken")
                .routeId("getTokenID")
                .log("CorrelationId=${exchangeProperty.correlationId} - Consultando token en Redis")
                .to("direct:redisGetToken")

                .choice()
                    .when(simple("${body} == null || ${body} == ''"))
                        .log("CorrelationId=${exchangeProperty.correlationId} - No hay token vigente en Redis, se solicita uno nuevo")
                        .to("direct:requestNewToken")
                    .otherwise()
                        .log("CorrelationId=${exchangeProperty.correlationId} - Token vigente encontrado en Redis")
                        .unmarshal().json(TokenAPI.class)
                .end()

                .log("CorrelationId=${exchangeProperty.correlationId} - Token final disponible: ${body.accessToken}");


        // Solicita un token nuevo al servicio externo (getToken) y lo persiste en Redis vía RedisRoute.
        from("direct:requestNewToken")
                .routeId("requestNewTokenID")
                .log("CorrelationId=${exchangeProperty.correlationId} - Solicitando token nuevo a getToken")
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
                .log("CorrelationId=${exchangeProperty.correlationId} - Token recibido: ${body.accessToken}")

                // direct:redisSaveToken sobrescribe el body con el resultado del SETEX,
                // así que guardamos el TokenAPI en una propiedad y lo restauramos después.
                .setProperty("newToken", body())
                .to("direct:redisSaveToken")
                .setBody(exchangeProperty("newToken"));
    }
}
