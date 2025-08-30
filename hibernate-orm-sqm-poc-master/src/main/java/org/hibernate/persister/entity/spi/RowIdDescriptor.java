/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.persister.entity.spi;

import org.hibernate.persister.common.spi.SingularAttribute;
import org.hibernate.persister.common.spi.VirtualAttribute;

/**
 * @author Steve Ebersole
 */
public interface RowIdDescriptor extends VirtualAttribute, SingularAttribute {
}
