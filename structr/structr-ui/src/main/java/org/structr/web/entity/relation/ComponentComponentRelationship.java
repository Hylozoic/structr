/*
 *  Copyright (C) 2012 Axel Morgner
 * 
 *  This file is part of structr <http://structr.org>.
 * 
 *  structr is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  structr is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with structr.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.structr.web.entity.relation;

import java.util.LinkedHashSet;
import java.util.Set;
import org.structr.common.PropertyKey;
import org.structr.common.RelType;
import org.structr.core.EntityContext;
import org.structr.core.entity.AbstractRelationship;
import org.structr.web.entity.Component;
import org.structr.web.entity.html.HtmlElement;

/**
 *
 * @author Christian Morgner
 */
public class ComponentComponentRelationship extends AbstractRelationship {

	public enum Key implements PropertyKey {
		parentId, componentId, resourceId
	}

	static {
		EntityContext.registerNamedRelation("component_component_relationship", ComponentComponentRelationship.class, Component.class, Component.class, RelType.CONTAINS);
		
		// not needed, overridden below
		// EntityContext.registerPropertySet(ContentRelationship.class, PropertyView.All,    ContentRelationship.Key.values());
		// EntityContext.registerPropertySet(ContentRelationship.class, PropertyView.Public, ContentRelationship.Key.values());
	}


	@Override
	public PropertyKey getStartNodeIdKey() {
		return Key.parentId;
	}

	@Override
	public PropertyKey getEndNodeIdKey() {
		return Key.componentId;
	}
	
	@Override
	public Iterable<String> getPropertyKeys(String propertyView) {
		
		Set<String> keys = new LinkedHashSet<String>();
		
		keys.add(Key.parentId.name());
		keys.add(Key.componentId.name());
		keys.add(Key.resourceId.name());
		
		if(dbRelationship != null) {
			
			for(String key : dbRelationship.getPropertyKeys()) {
				keys.add(key);
			}
		}
		
		return keys;
	}
}