/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.test.sql.convert;

import java.util.List;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import org.hibernate.boot.MetadataSources;
import org.hibernate.orm.test.sql.BaseUnitTest;
import org.hibernate.persister.common.internal.DerivedTable;
import org.hibernate.persister.common.internal.UnionSubclassTable;
import org.hibernate.persister.common.spi.Table;
import org.hibernate.sql.ast.QuerySpec;
import org.hibernate.sql.ast.SelectQuery;
import org.hibernate.sql.ast.from.FromClause;
import org.hibernate.sql.ast.from.TableBinding;
import org.hibernate.sql.ast.from.TableSpace;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * @author Andrea Boriero
 */
public class TablePerClassWithAbstractRootTest extends BaseUnitTest {

	@Test
	public void selectRootEntity() {
		final QuerySpec querySpec = getQuerySpec( "select e from RootEntity e" );

		final FromClause fromClause =
				querySpec.getFromClause();
		final List<TableSpace> tableSpaces = fromClause.getTableSpaces();
		assertThat( tableSpaces.size(), is( 1 ) );
		final TableSpace tableSpace = tableSpaces.get( 0 );

		assertThat( tableSpace.getJoinedTableGroups().size(), is( 0 ) );

		final TableBinding rootTableBinding = tableSpace.getRootTableGroup().getRootTableBinding();
		final Table table = rootTableBinding.getTable();
		assertThat( table, instanceOf( UnionSubclassTable.class ) );

		assertThat( table.getTableExpression(), containsString( "union all" ) );
		assertThat( table.getTableExpression(), containsString( "second_child" ) );
		assertThat( table.getTableExpression(), containsString( "first_child" ) );

		assertThat( tableSpace.getRootTableGroup().getTableJoins().size(), is( 0 ) );
	}

	@Override
	protected void applyMetadataSources(MetadataSources metadataSources) {
		metadataSources.addAnnotatedClass( RootEntity.class );
		metadataSources.addAnnotatedClass( FirstChild.class );
		metadataSources.addAnnotatedClass( SecondChild.class );
	}

	@Entity(name = "RootEntity")
	@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
	public abstract static class RootEntity {
		@Id
		public Integer id;
		public String name;
		public String description;
	}

	@Entity(name = "FirstChild")
	@javax.persistence.Table(name = "first_child")
	public static class FirstChild extends RootEntity {
		public String child1;
	}

	@Entity(name = "SecondChild")
	@javax.persistence.Table(name = "second_child")
	public static class SecondChild extends RootEntity {
		public String child2;
	}

	private QuerySpec getQuerySpec(String hql) {
		final SelectQuery selectQuery = interpretSelectQuery(hql).getSqlSelectAst();
		assertThat( selectQuery, notNullValue() );
		return selectQuery.getQuerySpec();
	}

}
