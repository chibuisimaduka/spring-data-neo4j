/**
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.data.neo4j.config;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.AbstractGraphDatabase;
import org.neo4j.kernel.Config;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.kernel.HighlyAvailableGraphDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.neo4j.model.PersonRepository;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import static org.junit.Assert.assertEquals;

/**
 * @author mh
 * @since 31.01.11
 */

public class DataGraphNamespaceHandlerTest {

    static class Config {
        @Autowired
        GraphDatabaseService graphDatabaseService;
        @Autowired
        Neo4jTemplate neo4jTemplate;
        @Autowired
        PlatformTransactionManager transactionManager;
        @Autowired(required = false)
        PersonRepository personRepository;
    }

    @Test
    public void injectionForJustStoreDir() {
        final Config config = assertInjected("");
        Assert.assertNotNull(config.personRepository);
    }
    @Test
    public void injectionForExistingGraphDatabaseService() {
        final Config config = assertInjected("-external-embedded");
        final AbstractGraphDatabase gds = (AbstractGraphDatabase) config.graphDatabaseService;
        assertEquals(EmbeddedGraphDatabase.class, gds.getClass());
        final org.neo4j.kernel.Config neoConfig = gds.getConfig();
        assertEquals("true", neoConfig.getParams().get("allow_store_upgrade"));
    }
    @Test
    @Ignore("todo setup zk-cluster")
    public void injectionForExistingHighlyAvailableGraphDatabaseService() {
        final Config config = assertInjected("-external-ha");
        final AbstractGraphDatabase gds = (AbstractGraphDatabase) config.graphDatabaseService;
        final org.neo4j.kernel.Config neoConfig = gds.getConfig();
        assertEquals(HighlyAvailableGraphDatabase.class, gds.getClass());
        assertEquals("1", neoConfig.getParams().get("ha.server_id"));
    }

    @Test
    public void injectionForCodeConfiguredExistingGraphDatabaseService() {
        assertInjected("-code");
    }

    private Config assertInjected(String testCase) {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:org/springframework/data/neo4j/config/DataGraphNamespaceHandlerTest" + testCase + "-context.xml");
        try {
        Config config = ctx.getBean("config", Config.class);
        Neo4jTemplate template = config.neo4jTemplate;
        Assert.assertNotNull("template", template);
        AbstractGraphDatabase graphDatabaseService = (AbstractGraphDatabase) template.getGraphDatabaseService();
        Assert.assertEquals("store-dir", "target/config-test", graphDatabaseService.getStoreDir());
        Assert.assertNotNull("graphDatabaseService",config.graphDatabaseService);
        Assert.assertNotNull("transactionManager",config.transactionManager);
        config.graphDatabaseService.shutdown();
        return config;
        } finally {
            ctx.close();
        }
    }

}
