package org.processmining.processcomparator.tbutils.dummycontext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.processmining.framework.connections.Connection;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.connections.ConnectionID;
import org.processmining.framework.connections.ConnectionManager;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.events.ConnectionObjectListener;


/**
 * Copied from the Stochastic Petri net package (org.processmining.plugins.stochasticpetrinet.StochasticNetUtils).
 * 
 * @author sander leemans
 */
public class DummyConnectionManager implements ConnectionManager {
  private final Map<ConnectionID, Connection> connections = new HashMap<ConnectionID, Connection>();

  public DummyConnectionManager() {
  }

  public void setEnabled(boolean isEnabled) {
  }

  public boolean isEnabled() {
    return false;
  }

  public <T extends Connection> T getFirstConnection(Class<T> connectionType, PluginContext context, Object... objects)
      throws ConnectionCannotBeObtained {
    Iterator<Map.Entry<ConnectionID, Connection>> it = connections.entrySet().iterator();
    while (it.hasNext()) {
      Entry<ConnectionID, Connection> entry = it.next();
      Connection c = entry.getValue();
      if (((connectionType == null) || connectionType.isAssignableFrom(c.getClass())) && c.containsObjects(objects)) {
        return (T) c;
      }
    }
    throw new ConnectionCannotBeObtained("Connections can't be obtained in dummy testing", connectionType, objects);
  }

  public <T extends Connection> Collection<T> getConnections(Class<T> connectionType, PluginContext context,
      Object... objects) throws ConnectionCannotBeObtained {
    Collection<T> validConnections = new ArrayList<>();
    Iterator<Map.Entry<ConnectionID, Connection>> it = connections.entrySet().iterator();
    while (it.hasNext()) {
      Entry<ConnectionID, Connection> entry = it.next();
      Connection c = entry.getValue();
      if (((connectionType == null) || connectionType.isAssignableFrom(c.getClass())) && c.containsObjects(objects)) {
        validConnections.add((T) c);
      }
    }
    return validConnections;
  }

  public org.processmining.framework.plugin.events.ConnectionObjectListener.ListenerList getConnectionListeners() {
    org.processmining.framework.plugin.events.ConnectionObjectListener.ListenerList list = new ConnectionObjectListener.ListenerList();
    return list;
  }

  public Collection<ConnectionID> getConnectionIDs() {
    java.util.List<ConnectionID> list = new ArrayList<>();
    return list;
  }

  public Connection getConnection(ConnectionID id) throws ConnectionCannotBeObtained {
    if (connections.containsKey(id)) {
      return connections.get(id);
    }
    throw new ConnectionCannotBeObtained("No connection with id " + id.toString(), null);
  }

  public void clear() {
    this.connections.clear();
  }

  public <T extends Connection> T addConnection(T connection) {
    connections.put(connection.getID(), connection);
    connection.setManager(this);
    return connection;
  }
}
