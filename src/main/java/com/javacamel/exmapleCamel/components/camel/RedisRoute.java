package com.javacamel.exmapleCamel.components.camel;


import com.javacamel.exmapleCamel.beans.TokenAPI;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class RedisRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {

        // Lee el token almacenado en Redis (key real de producción).
        // El body queda en null si la key no existe o si ya expiró (Redis la elimina solo).
        from("direct:redisGetToken")
                .routeId("redisGetTokenID")
                .setHeader("CamelRedis.Key", simple("{{redis.token.key}}"))
                .to("spring-redis:localhost:6379?command=GET")
                .log(LoggingLevel.DEBUG, "Token leído de Redis (key=${header.CamelRedis.Key}): ${body}");


        // Guarda en Redis (key real) un TokenAPI recibido en el body, con expiración (SETEX).
        // La usa TokenRoute cada vez que obtiene un token nuevo desde el servicio externo.
        from("direct:redisSaveToken")
                .routeId("redisSaveTokenID")
                .setHeader("CamelRedis.Key", simple("{{redis.token.key}}"))
                // Guardamos el TTL como propiedad ANTES de marshalear,
                // porque después de convertir a JSON/String ya no tenemos el objeto TokenAPI
                .setProperty("tokenTtl", simple("${body.expiresIn}"))
                .marshal().json()
                .convertBodyTo(String.class)
                .setHeader("CamelRedis.Value", body())
                // Header requerido por el comando SETEX: TTL en segundos (Long)
                .setHeader("CamelRedis.Timeout", exchangeProperty("tokenTtl"))
                // command=SETEX para que la key aplique expiración
                .to("spring-redis:localhost:6379?command=SETEX&redisTemplate=#redisTemplate")
                .log("Token guardado en Redis (key=${header.CamelRedis.Key}, ttl=${header.CamelRedis.Timeout}s)");


        // --- Ruta de prueba manual (no forma parte del flujo real, se deja para pruebas aisladas) ---
        // Confirma que SETEX + expiración funcionan en Redis, usando una key de prueba
        // distinta a la key real de producción para no pisar el token real.
        from("direct:testRedis")
                .routeId("testRedisID")
                .setHeader("CamelRedis.Key", simple("{{redis.token.test-key}}"))
                .setBody(exchange -> {
                    TokenAPI token = new TokenAPI();
                    token.setAccessToken("TOKEN-DE-PRUEBA-123456");
                    token.setExpiresIn(3600);
                    return token;
                })
                .setProperty("tokenTtl", simple("${body.expiresIn}"))
                .marshal().json()
                .convertBodyTo(String.class)
                .setHeader("CamelRedis.Value", body())
                .setHeader("CamelRedis.Timeout", exchangeProperty("tokenTtl"))
                .to("spring-redis:localhost:6379?command=SETEX&redisTemplate=#redisTemplate")
                .log("Token guardado en Redis (key=${header.CamelRedis.Key}, ttl=${header.CamelRedis.Timeout}s)");
    }
}
