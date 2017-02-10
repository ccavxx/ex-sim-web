package com.topsec.tsm.sim.test.service;

import org.junit.Assert;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.Test;

import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.resource.persistence.Node;
import com.topsec.tsm.sim.test.BaseTest;

public class NodeMgrServiceTest extends BaseTest<NodeMgrFacade>{

	@Test
	public void testGetNodeById(){
		Node node = testInstance.getNodeByNodeId("15e9f27e-e144-4512-b31b-a56f08aca0dc", true, true, true, true) ;
		Assert.assertNotNull(node) ;
	}
}
