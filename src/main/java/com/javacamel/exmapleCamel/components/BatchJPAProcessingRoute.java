package com.javacamel.exmapleCamel.components;

import com.javacamel.exmapleCamel.beans.NameAddress;
import com.javacamel.exmapleCamel.procesor.InboundMessageProcessor;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class BatchJPAProcessingRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        from("timer:readBD?period=5000")
                .routeId("readDBId")
                .to("jpa:"+NameAddress.class.getName()+"?namedQuery=fetchAllRows")
                .split(body())
                .process(new InboundMessageProcessor() )
                .convertBodyTo(String.class)
                .to("file:src/data/output?fileName=outputFile.csv&fileExist=append&appendChars=\\n")
                .toD("jpa:"+NameAddress.class.getName()+"?nativeQuery=DELETE FROM name_address WHERE ID =${header.consumerId}&useExecuteUpdate=true")
                .end();


    }
}
