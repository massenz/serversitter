package com.rivermeadow.babysitter.zookeper;

import com.rivermeadow.babysitter.model.Server;
import org.apache.zookeeper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

public class NodesManager implements Watcher, BeatListener {

    private static final int SESSION_TIMEOUT = 5000;
    Logger logger = Logger.getLogger("NodesManager");
    private ZooKeeper zk;
    private CountDownLatch connectedSignal = new CountDownLatch(1);

    public NodesManager() {
        try {
            // TODO: get the list of zookeper servers from a configuration class
            connect("localhost");
        } catch (IOException | InterruptedException e) {
            logger.severe("Could not connect to Zookeper instance");
        }
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
            connectedSignal.countDown();
        }
    }

    public void connect(String hosts) throws IOException, InterruptedException {
        zk = new ZooKeeper(hosts, SESSION_TIMEOUT, this);
        connectedSignal.await();
    }

    public void create(String name) throws KeeperException, InterruptedException {
        String path = File.separator + name;
        String createdPath = zk.create(path, null, ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT);
        logger.info(String.format("Created %s", createdPath));
    }

    @Override
    public void register(Server server) {
        try {
            create(server.getServerAddress().getHostname());
            // TODO: add server data to zk node
        } catch (KeeperException | InterruptedException e) {
            logger.severe(String.format("Could not register server: %s [%s]" +
                    server.getServerAddress().getHostname(), e.getLocalizedMessage()));
            // TODO: silently ignore?
        }
    }

    @Override
    public void updateHeartbeat(Server server) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void deregister(Server server) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
