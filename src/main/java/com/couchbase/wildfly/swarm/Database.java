package com.couchbase.wildfly.swarm;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.JsonLongDocument;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;

/**
 * @author Arun Gupta
 */
@Singleton
@Startup
public class Database {
    
    CouchbaseCluster cluster;
    Bucket bucket;
    
    @PostConstruct
    public void init() {
        if (!getBucket().exists("airline_sequence")) {
            N1qlQuery query = N1qlQuery.simple("SELECT MAX(id) + 1 as counterInit FROM `travel-sample` where type=\"airline\"");
            N1qlQueryResult result = bucket.query(query);
            if (result.finalSuccess()) {
                long counterInit = result.allRows().get(0).value().getLong("counterInit");
                bucket.insert(JsonLongDocument.create("airline_sequence", counterInit));
            }
        }
    }
    
    @PreDestroy
    public void stop() {
        bucket.close();
        cluster.disconnect();
    }

    public CouchbaseCluster getCluster() {
        if (null == cluster) {
            String couchbaseURI = System.getenv("COUCHBASE_URI");
            if (null == couchbaseURI) {
                System.err.println("WARING: No COUCHBASE_URI specified, defaulting to 192.168.99.100");
                couchbaseURI = "192.168.99.100";
            }
            System.out.println("Couchbase endpoint: " + System.getenv("COUCHBASE_URI"));
            cluster = CouchbaseCluster.create(couchbaseURI);
        }
        return cluster;
    }
    
    public Bucket getBucket() {
        if (null == bucket) {
            bucket = getCluster().openBucket("travel-sample");
        }
        return bucket;
    }
}