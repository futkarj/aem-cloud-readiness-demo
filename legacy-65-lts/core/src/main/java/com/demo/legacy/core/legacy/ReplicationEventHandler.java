package com.demo.legacy.core.legacy;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.replication.ReplicationAction;

/**
 * LEGACY: an OSGi EventHandler on the replication topic doing work inline. Cloud Service guidance
 * is to hand this to a Sling Job so it survives restarts and runs once in the cluster.
 */
@Component(service = EventHandler.class, immediate = true,
        property = EventConstants.EVENT_TOPIC + "=" + ReplicationAction.EVENT_TOPIC)
public class ReplicationEventHandler implements EventHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ReplicationEventHandler.class);

    @Override
    public void handleEvent(Event event) {
        ReplicationAction action = ReplicationAction.fromEvent(event);
        if (action != null) {
            LOG.info("Replicated {} {}", action.getType(), action.getPath());
        }
    }
}
