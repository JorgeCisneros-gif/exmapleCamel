package com.javacamel.exmapleCamel.beans;

import org.apache.camel.Exchange;

public class InboundRestProcessingbean {
    public void validate(Exchange exchange) throws Exception {
        NameAddress address = exchange.getIn().getBody(NameAddress.class);
        exchange.getIn().setHeader("userCity", address.getCity());

    }
}
