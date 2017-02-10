package com.topsec.tsm.sim.test.util;

import java.util.Collections;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.junit.Test;

import com.topsec.tsm.sim.webservice.xmltype.DataSourceTypeElement;
import com.topsec.tsm.sim.webservice.xmltype.DataSourceTypeListElement;

public class JaxbTest {

	
	@Test
	public void test1() throws JAXBException{
		JAXBContext context = JAXBContext.newInstance(DataSourceTypeListElement.class) ;
		Marshaller mar = context.createMarshaller() ;
		DataSourceTypeListElement obj = new DataSourceTypeListElement(Collections.<DataSourceTypeElement>emptyList()) ;
		mar.marshal(obj, System.out);
	}
}
