package com.javacamel.exmapleCamel.components;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class QueueReciveRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        //from("activemq6:queue:nameaddressqueue"
        //        + "?deserializationFilter=!java.net.**;java.**;javax.**;org.apache.camel.**;com.javacamel.exmapleCamel.beans.**;!*")
         from("activemq6:queue:nameaddressqueue")
                .log(LoggingLevel.INFO,">>>>>>>>>Message Received from Queue: ${body}");
    }
}
