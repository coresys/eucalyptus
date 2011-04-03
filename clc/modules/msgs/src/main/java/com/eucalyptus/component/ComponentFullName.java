/*******************************************************************************
 * Copyright (c) 2009  Eucalyptus Systems, Inc.
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, only version 3 of the License.
 * 
 * 
 *  This file is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  for more details.
 * 
 *  You should have received a copy of the GNU General Public License along
 *  with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 *  Please contact Eucalyptus Systems, Inc., 130 Castilian
 *  Dr., Goleta, CA 93101 USA or visit <http://www.eucalyptus.com/licenses/>
 *  if you need additional information or have any questions.
 * 
 *  This file may incorporate work covered under the following copyright and
 *  permission notice:
 * 
 *    Software License Agreement (BSD License)
 * 
 *    Copyright (c) 2008, Regents of the University of California
 *    All rights reserved.
 * 
 *    Redistribution and use of this software in source and binary forms, with
 *    or without modification, are permitted provided that the following
 *    conditions are met:
 * 
 *      Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 * 
 *      Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 * 
 *    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 *    IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 *    TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 *    PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 *    OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 *    EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 *    PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 *    PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 *    LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *    NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *    SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. USERS OF
 *    THIS SOFTWARE ACKNOWLEDGE THE POSSIBLE PRESENCE OF OTHER OPEN SOURCE
 *    LICENSED MATERIAL, COPYRIGHTED MATERIAL OR PATENTED MATERIAL IN THIS
 *    SOFTWARE, AND IF ANY SUCH MATERIAL IS DISCOVERED THE PARTY DISCOVERING
 *    IT MAY INFORM DR. RICH WOLSKI AT THE UNIVERSITY OF CALIFORNIA, SANTA
 *    BARBARA WHO WILL THEN ASCERTAIN THE MOST APPROPRIATE REMEDY, WHICH IN
 *    THE REGENTS’ DISCRETION MAY INCLUDE, WITHOUT LIMITATION, REPLACEMENT
 *    OF THE CODE SO IDENTIFIED, LICENSING OF THE CODE SO IDENTIFIED, OR
 *    WITHDRAWAL OF THE CODE CAPABILITY TO THE EXTENT NEEDED TO COMPLY WITH
 *    ANY SUCH LICENSES OR RIGHTS.
 *******************************************************************************
 * @author chris grzegorczyk <grze@eucalyptus.com>
 */

package com.eucalyptus.component;

import java.util.Arrays;
import java.util.List;
import com.eucalyptus.component.id.Eucalyptus;
import com.eucalyptus.util.Assertions;
import com.eucalyptus.util.FullName;
import com.google.common.collect.Lists;

public class ComponentFullName implements FullName {
  public final static String VENDOR = "euca";
  private final ComponentId  componentId;
  private final String       partition;
  private final String       name;
  private final String       qName;
  private final String       authority;
  private final String       relativeId;
  
  ComponentFullName( ComponentId componentType, String partition, String name, String... pathPartsArray ) {
    Assertions.assertNotNull( componentType );
    Assertions.assertNotNull( partition );
    Assertions.assertNotNull( name );
    this.componentId = componentType;
    this.partition = partition;
    this.name = name;
    List<String> temp = Lists.newArrayList( );
    if ( componentType != null ) {
      temp.add( componentType.name( ) );
    } else {
      temp.add( ComponentIds.lookup( Eucalyptus.class ).name( ) );
    }
    temp.add( name );
    temp.addAll( Arrays.asList( pathPartsArray ) );
    this.authority = new StringBuilder( ).append( PREFIX ).append( SEP ).append( VENDOR ).append( SEP ).append( partition ).append( SEP ).append( this.componentId.getName( ) ).append( SEP ).toString( );
    StringBuilder rId = new StringBuilder( );
    for ( String pathPart : pathPartsArray ) {
      rId.append( SEP_PATH.substring( 0, rId.length( ) == 0 ? 0 : 1 ) ).append( pathPart );
    }
    this.relativeId = rId.toString( );
    this.qName = this.authority + this.relativeId;
  }
  
  @Override
  public final String getVendor( ) {
    return VENDOR;
  }
  
  @Override
  public final String getRegion( ) {
    return this.getPartition( );
  }
  
  @Override
  public final String getNamespace( ) {
    return this.componentId.getName( );
  }
  
  @Override
  public final String getAuthority( ) {
    return this.authority;
  }
  
  @Override
  public final String getRelativeId( ) {
    return this.relativeId;
  }
  
  @Override
  public final String getPartition( ) {
    return this.partition;
  }
  
  @Override
  public final String getName( ) {
    return this.name;
  }
  
  @Override
  public String toString( ) {
    return this.qName;
  }
  
  @Override
  public int hashCode( ) {
    final int prime = 31;
    int result = 1;
    result = prime * result + ( ( this.name == null )
      ? 0
      : this.name.hashCode( ) );
    result = prime * result + ( ( this.partition == null )
      ? 0
      : this.partition.hashCode( ) );
    return result;
  }
  
  @Override
  public boolean equals( Object obj ) {
    if ( this == obj ) {
      return true;
    }
    if ( obj == null ) {
      return false;
    }
    if ( !this.getClass( ).equals( obj.getClass( ) ) ) {
      return false;
    }
    
    ComponentFullName that = ( ComponentFullName ) obj;
    if ( this.name == null ) {
      if ( that.name != null ) {
        return false;
      }
    } else if ( !this.name.equals( that.name ) ) {
      return false;
    }
    if ( this.partition == null ) {
      if ( that.partition != null ) {
        return false;
      }
    } else if ( !this.partition.equals( that.partition ) ) {
      return false;
    }
    return true;
  }
  
  @Override
  public String getUniqueId( ) {
    return this.qName;
  }

  @Override
  public String getFullyQualifiedName( ) {
    return this.toString( );
  }
  
}