package com.javacamel.exmapleCamel.components;


import com.javacamel.exmapleCamel.beans.TokenAPI;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class RedisRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {

        from("direct:redisGetToken")
                .routeId("redisGetTokenID")

                .setHeader("CamelRedis.Key", constant("camel:token"))

                .log("Antes de Redis - Key: ${header.CamelRedis.Key}")
                .log("Antes de Redis - Body: ${body}")

                .to("spring-redis:localhost:6379?command=GET")

                .log("Después de Redis - Body: ${body}");


        from("direct:testRedis")
                .routeId("testRedisID")

                .setHeader("CamelRedis.Key")
                .constant("camel:token1")

                .setBody(exchange -> {
                    TokenAPI token = new TokenAPI();

                    token.setAccessToken("TOKEN-DE-PRUEBA-123456");
                    token.setExpiresIn(3600);

                    return token;
                })

                .log("TOKEN OBJECT = ${body}")
                .log("BODY TYPE = ${bodyType}")

                .marshal().json()

                .convertBodyTo(String.class)

                .setHeader("CamelRedis.Value")
                .body()

                .log("KEY = ${header.CamelRedis.Key}")
                .log("JSON A REDIS = ${header.CamelRedis.Value}")
                .log("BODY TYPE DESPUES JSON = ${bodyType}")

                .to("spring-redis:localhost:6379?command=SET&redisTemplate=#redisTemplate")

                .log("TOKEN OBJECT GUARDADO EN REDIS");
    }
}