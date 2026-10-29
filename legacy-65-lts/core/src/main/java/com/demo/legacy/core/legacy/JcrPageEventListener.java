package com.demo.legacy.core.legacy;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;

import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LEGACY: a long-lived administrative JCR session holding a javax.jcr EventListener. Cloud Service
 * expects ResourceChangeListener or Sling Jobs, and no administrative logins.
 */
@Component(immediate = true)
public class JcrPageEventListener implements EventListener {

    private static final Logger LOG = LoggerFactory.getLogger(JcrPageEventListener.class);

    @Reference
    private SlingRepository repository;

    private Session session;

    @Activate
    @SuppressWarnings("deprecation")
    protected void activate() throws RepositoryException {
        session = repository.loginAdministrative(null);
        session.getWorkspace().getObservationManager().addEventListener(this,
                Event.NODE_ADDED | Event.PROPERTY_CHANGED, "/content/legacydemo", true, null, null, false);
    }

    @Deactivate
    protected void deactivate() throws RepositoryException {
        if (session != null) {
            session.getWorkspace().getObservationManager().removeEventListener(this);
            session.logout();
        }
    }

    @Override
    public void onEvent(EventIterator events) {
        while (events.hasNext()) {
            try {
                LOG.info("Content changed: {}", events.nextEvent().getPath());
            } catch (RepositoryException e) {
                LOG.warn("Could not read event path", e);
            }
        }
    }
}
