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


package org.structr.core.graph;

import java.util.Collections;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import org.structr.core.entity.AbstractNode;
import org.structr.core.entity.AbstractRelationship;

//~--- JDK imports ------------------------------------------------------------

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.structr.common.error.FrameworkException;

//~--- classes ----------------------------------------------------------------

/**
 * Returns a List of relationships for the given node.
 *
 * @param one or more AbstractNode instances to collect the properties from.
 * @return a list of relationships for the given nodes
 *
 * @author Axel Morgner
 */
public class NodeRelationshipsCommand extends NodeServiceCommand {

	private static final Logger logger = Logger.getLogger(NodeRelationshipsCommand.class.getName());

	//~--- methods --------------------------------------------------------

	/**
	 * Fetch relationships for the given source node.
	 * 
	 * @param sourceNode
	 * @param relType can be null
	 * @param dir
	 * 
	 * @return a list of relationships
	 * @throws FrameworkException 
	 */
	public List<AbstractRelationship> execute(AbstractNode sourceNode, RelationshipType relType, Direction dir) throws FrameworkException {

		RelationshipFactory factory       = new RelationshipFactory(securityContext);
		List<AbstractRelationship> result = new LinkedList<AbstractRelationship>();
		Node node                         = sourceNode.getNode();
		Iterable<Relationship> rels;

		if (node == null) {
			
			return Collections.EMPTY_LIST;
			
		}
		
		if (relType != null) {
			
			rels = node.getRelationships(relType, dir);
			
		} else {
			
			rels = node.getRelationships(dir);
		}

		try {

			for (Relationship r : rels) {
				
				result.add(factory.instantiateRelationship(securityContext, r));
			}

		} catch (RuntimeException e) {

			logger.log(Level.WARNING, "Exception occured: ", e.getMessage());

			/**
				* ********* FIXME 
				*
				* Here an exception occurs:
				*
				* org.neo4j.kernel.impl.nioneo.store.InvalidRecordException: Node[5] is neither firstNode[37781] nor secondNode[37782] for Relationship[188125]
				* at org.neo4j.kernel.impl.nioneo.xa.ReadTransaction.getMoreRelationships(ReadTransaction.java:131)
				* at org.neo4j.kernel.impl.nioneo.xa.NioNeoDbPersistenceSource$ReadOnlyResourceConnection.getMoreRelationships(NioNeoDbPersistenceSource.java:280)
				* at org.neo4j.kernel.impl.persistence.PersistenceManager.getMoreRelationships(PersistenceManager.java:100)
				* at org.neo4j.kernel.impl.core.NodeManager.getMoreRelationships(NodeManager.java:585)
				* at org.neo4j.kernel.impl.core.NodeImpl.getMoreRelationships(NodeImpl.java:358)
				* at org.neo4j.kernel.impl.core.IntArrayIterator.hasNext(IntArrayIterator.java:115)
				*
				*
				*/
		}
		
		return result;
	}
}
