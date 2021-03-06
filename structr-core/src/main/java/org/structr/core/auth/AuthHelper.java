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


package org.structr.core.auth;

import org.apache.commons.codec.digest.DigestUtils;

import org.structr.common.SecurityContext;
import org.structr.common.error.FrameworkException;
import org.structr.core.Services;
import org.structr.core.auth.exception.AuthenticationException;
import org.structr.core.entity.Principal;
import org.structr.core.entity.SuperUser;
import org.structr.core.graph.search.Search;
import org.structr.core.graph.search.SearchAttribute;
import org.structr.core.graph.search.SearchNodeCommand;

//~--- JDK imports ------------------------------------------------------------

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;
import org.structr.core.Result;
import org.structr.core.entity.AbstractNode;
import org.structr.core.entity.Person;
import org.structr.core.graph.search.SearchAttributeGroup;
import org.structr.core.graph.search.SearchOperator;
import org.structr.core.property.PropertyKey;

//~--- classes ----------------------------------------------------------------

/**
 * Utility class for authentication
 *
 * @author Axel Morgner
 */
public class AuthHelper {

	private static final String STANDARD_ERROR_MSG = "Wrong username or password, or user is blocked. Check caps lock. Note: Username is case sensitive!";
	private static final Logger logger             = Logger.getLogger(AuthHelper.class.getName());

	//~--- get methods ----------------------------------------------------

	/**
	 * Find a {@link Principal} for the given email address
	 * 
	 * @param securityContext
	 * @param email
	 * @return 
	 */
	public static Principal getPrincipalForEmail(final String email) {
		
		Principal principal = null;
		
		Result result = Result.EMPTY_RESULT;
		try {
			
			result = Services.command(SecurityContext.getSuperUserInstance(), SearchNodeCommand.class).execute(
				Search.andExactTypeAndSubtypes(Principal.class.getSimpleName()),
				Search.andExactProperty(Person.email, email));

		} catch (FrameworkException ex) {
			
			logger.log(Level.WARNING, "Could not search for person", ex);

		}

		if (!result.isEmpty()) {

			principal = (Principal) result.get(0);

		}

		return principal;
	}
	
	/**
	 * Find a {@link Principal} with matching password and given key or name
	 * 
	 * @param securityContext
	 * @param key
	 * @param value
	 * @param password
	 * @return
	 * @throws AuthenticationException 
	 */
	public static Principal getPrincipalForPassword(final PropertyKey key, final String value, final String password) throws AuthenticationException {

		String errorMsg = null;
		Principal principal  = null;

		if (Services.getSuperuserUsername().equals(value) && Services.getSuperuserPassword().equals(password)) {

			logger.log(Level.INFO, "############# Authenticated as superadmin! ############");

			principal = new SuperUser();

		} else {

			try {

				SearchNodeCommand searchNode = Services.command(SecurityContext.getSuperUserInstance(), SearchNodeCommand.class);
				List<SearchAttribute> attrs  = new LinkedList<SearchAttribute>();

				attrs.add(Search.andExactTypeAndSubtypes(Principal.class.getSimpleName()));
				SearchAttributeGroup group = new SearchAttributeGroup(SearchOperator.AND);
				group.add(Search.orExactProperty(key, value));
				group.add(Search.orExactProperty(AbstractNode.name, value));
				attrs.add(group);

				Result userList = searchNode.execute(attrs);
				
				if (!userList.isEmpty()) {
					principal = (Principal) userList.get(0);
				}

				if (principal == null) {

					logger.log(Level.INFO, "No user found for {0} {1}", new Object[]{ key.dbName(), value });

					errorMsg = STANDARD_ERROR_MSG;

				} else {

					if (principal.isBlocked()) {

						logger.log(Level.INFO, "User {0} is blocked", principal);

						errorMsg = STANDARD_ERROR_MSG;

					}

					if (StringUtils.isEmpty(password)) {

						logger.log(Level.INFO, "Empty password for principal {0}", principal);

						errorMsg = "Empty password, should not ever happen down here!";

					} else {

						String encryptedPasswordValue = DigestUtils.sha512Hex(password);
						String pw                     = principal.getEncryptedPassword();

						if (pw == null || !encryptedPasswordValue.equals(pw)) {

							logger.log(Level.INFO, "Wrong password for principal {0}", principal);

							errorMsg = STANDARD_ERROR_MSG;

						}
					
					}

				}

			} catch (FrameworkException fex) {

				fex.printStackTrace();

			}

		}

		if (errorMsg != null) {

			throw new AuthenticationException(errorMsg);
		}

		return principal;

	}

	/**
	 * Find a {@link Principal} for the given session id
	 * 
	 * @param sessionId
	 * @return 
	 */
	public static Principal getPrincipalForSessionId(final String sessionId) {

		Principal user              = null;
		List<SearchAttribute> attrs = new LinkedList<SearchAttribute>();

		attrs.add(Search.andExactProperty(Principal.sessionId, sessionId));
		attrs.add(Search.andExactTypeAndSubtypes(Principal.class.getSimpleName()));

		try {

			// we need to search with a super user security context here..
			Result results = Services.command(SecurityContext.getSuperUserInstance(), SearchNodeCommand.class).execute(attrs);

			if (!results.isEmpty()) {

				user = (Principal) results.get(0);

				if ((user != null) && sessionId.equals(user.getProperty(Principal.sessionId))) {

					return user;
				}

			}
		} catch (FrameworkException fex) {

			logger.log(Level.WARNING, "Error while executing SearchNodeCommand", fex);

		}

		return user;

	}

}
