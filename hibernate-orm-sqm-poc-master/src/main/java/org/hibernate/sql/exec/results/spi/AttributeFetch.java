/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec.results.spi;

import org.hibernate.persister.common.spi.Attribute;
import org.hibernate.sql.convert.results.spi.Fetch;

/**
 * Models a fetch that is specifically for an attribute.
 *
 * @author Gail Badner
 */
public interface AttributeFetch extends Fetch {

	/**
	 * Returns the descriptor for attribute being fetched.
	 *
	 * @return The fetched attribute descriptor.
	 */
	Attribute getFetchedAttributeDescriptor();
}
