package com.eucalyptus.component;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import com.eucalyptus.records.EventRecord;
import com.eucalyptus.records.EventType;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Formerly known as {@link DefaultServiceBuilder}
 */
public class DummyServiceBuilder implements ServiceBuilder<ServiceConfiguration> {
  private Map<String, ServiceConfiguration> services = Maps.newConcurrentMap( );
  private final ComponentId                   component;
  
  DummyServiceBuilder( ComponentId component ) {
    this.component = component;
  }
  
  @Override
  public Boolean checkRemove( String partition, String name ) throws ServiceRegistrationException {
    return this.services.containsKey( name );
  }
  
  @Override
  public ComponentId getComponentId( ) {
    return this.component;
  }
  
  @Override
  public Boolean checkAdd( String partition, String name, String host, Integer port ) throws ServiceRegistrationException {
    return !this.services.containsKey( name );
  }
  
  @Override
  public ServiceConfiguration newInstance( String partition, String name, String host, Integer port ) {
    ComponentId compId = this.getComponentId( );
    return ServiceConfigurations.createEphemeral( compId, compId.getPartition( ), compId.name( ), ServiceUris.internal( compId ) );
  }
  
  @Override
  public ServiceConfiguration newInstance( ) {
    ComponentId compId = this.getComponentId( );
    return ServiceConfigurations.createEphemeral( compId, compId.getPartition( ), compId.name( ), compId.getLocalEndpointUri( ) );
  }
  
  @Override
  public void fireStart( ServiceConfiguration config ) throws ServiceRegistrationException {
    EventRecord.here( ServiceBuilder.class, EventType.COMPONENT_SERVICE_START, config.getFullName( ).toString( ), config.toString( ) ).exhaust( );
  }
  
  @Override
  public void fireStop( ServiceConfiguration config ) throws ServiceRegistrationException {
    EventRecord.here( ServiceBuilder.class, EventType.COMPONENT_SERVICE_STOP, config.getFullName( ).toString( ), config.toString( ) ).exhaust( );
  }
  
  @Override
  public void fireEnable( ServiceConfiguration config ) throws ServiceRegistrationException {
    EventRecord.here( ServiceBuilder.class, EventType.COMPONENT_SERVICE_ENABLE, config.getFullName( ).toString( ), config.toString( ) ).exhaust( );
  }
  
  @Override
  public void fireDisable( ServiceConfiguration config ) throws ServiceRegistrationException {
    EventRecord.here( ServiceBuilder.class, EventType.COMPONENT_SERVICE_DISABLE, config.getFullName( ).toString( ), config.toString( ) ).exhaust( );
  }
  
  @Override
  public void fireCheck( ServiceConfiguration config ) throws ServiceRegistrationException {
    EventRecord.here( ServiceBuilder.class, EventType.COMPONENT_SERVICE_CHECK, config.getFullName( ).toString( ), config.toString( ) ).exhaust( );
  }
}
