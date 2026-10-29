package com.demo.legacy.core.legacy;

import javax.jcr.Session;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.day.cq.replication.AgentFilter;
import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.ReplicationException;
import com.day.cq.replication.ReplicationOptions;
import com.day.cq.replication.Replicator;

/**
 * LEGACY: activates pages through one named replication agent ("legacy-flush"), selected with an
 * AgentFilter. Cloud Service replaces replication agents with Sling Content Distribution.
 */
@Component(service = PageActivationService.class)
public class PageActivationService {

    static final String AGENT_ID = "legacy-flush";

    @Reference
    private Replicator replicator;

    public void activate(Session session, String path) throws ReplicationException {
        ReplicationOptions options = new ReplicationOptions();
        options.setSynchronous(true);
        options.setFilter(new AgentFilter() {
            @Override
            public boolean isIncluded(com.day.cq.replication.Agent agent) {
                return AGENT_ID.equals(agent.getId());
            }
        });
        replicator.replicate(session, ReplicationActionType.ACTIVATE, path, options);
    }
}
