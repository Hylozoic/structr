/**
 * Copyright (C) 2010-2013 Axel Morgner, structr <structr@structr.org>
 *
 * This file is part of structr <http://structr.org>.
 *
 * structr is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * structr is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with structr.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.structr.common.error;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.structr.core.property.PropertyKey;

/**
 * Indicates an invalid search field.
 *
 * @author Christian Morgner
 */
public class InvalidSearchField extends SemanticErrorToken {

	public InvalidSearchField(PropertyKey propertyKey) {
		super(propertyKey);
	}

	@Override
	public JsonElement getContent() {

		JsonObject obj = new JsonObject();

		obj.add(getErrorToken(), new JsonPrimitive(getKey() ));

		return obj;
	}

	@Override
	public String getErrorToken() {
		return "invalid_search_key";
	}
}
