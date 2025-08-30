/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.test.sql.convert;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

import org.hibernate.boot.MetadataSources;
import org.hibernate.persister.common.internal.PhysicalTable;
import org.hibernate.persister.entity.JoinedSubclassEntityPersister;
import org.hibernate.persister.entity.spi.ImprovedEntityPersister;
import org.hibernate.sql.ast.QuerySpec;
import org.hibernate.sql.ast.from.EntityTableGroup;
import org.hibernate.sql.ast.from.TableBinding;
import org.hibernate.sql.convert.internal.FromClauseIndex;
import org.hibernate.sql.convert.internal.SqlAliasBaseManager;
import org.hibernate.orm.test.sql.BaseUnitTest;
import org.hibernate.sqm.query.SqmSelectStatement;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Steve Ebersole
 */
public class JoinedEntitySimpleTest extends BaseUnitTest {
	@Test
	public void testSingleSpaceBase() {
		SqmSelectStatement sqm = (SqmSelectStatement) interpret( "from JoinedEntityBase" );

		final ImprovedEntityPersister improvedEntityPersister =
				(ImprovedEntityPersister) getConsumerContext().getDomainMetamodel().resolveEntityReference( "JoinedEntityBase" );
		assertThat( improvedEntityPersister.getEntityPersister(), instanceOf( JoinedSubclassEntityPersister.class ) );

		// interpreter set up
		final QuerySpec querySpec = new QuerySpec();
		final SqlAliasBaseManager aliasBaseManager = new SqlAliasBaseManager();
		final FromClauseIndex fromClauseIndex = new FromClauseIndex();

		final EntityTableGroup result = improvedEntityPersister.buildTableGroup(
				sqm.getQuerySpec().getFromClause().getFromElementSpaces().get( 0 ).getRoot(),
				querySpec.getFromClause().makeTableSpace(),
				aliasBaseManager,
				fromClauseIndex
		);
		assertThat( result, notNullValue() );
		assertThat( result.getAliasBase(), equalTo( "j1" ) );

		assertThat( result.getRootTableBinding(), notNullValue() );
		assertThat( result.getRootTableBinding().getTable(), instanceOf( PhysicalTable.class ) );
		final PhysicalTable tableSpec = (PhysicalTable) result.getRootTableBinding().getTable();
		assertThat( tableSpec.getTableName(), equalTo( "joined_entity_base" ) );
		assertThat( result.getRootTableBinding().getIdentificationVariable(), equalTo( "j1" ) );

		assertThat( result.getTableJoins().size(), equalTo( 2 ) );

		assertThat(
				result.getTableJoins().get( 0 ).getJoinedTableBinding().getTable(),
				instanceOf( PhysicalTable.class )
		);
		final TableBinding firstSubclassTableBinding = result.getTableJoins().get( 0 ).getJoinedTableBinding();
		assertThat( firstSubclassTableBinding.getTable().getTableExpression(), equalTo( "joined_entity_branch" ) );
		assertThat( firstSubclassTableBinding.getIdentificationVariable(), equalTo( "j1_0" ) );

		assertThat(
				result.getTableJoins().get( 1 ).getJoinedTableBinding().getTable(),
				instanceOf( PhysicalTable.class )
		);
		final TableBinding secondSubclassTableBinding = result.getTableJoins().get( 1 ).getJoinedTableBinding();
		assertThat( secondSubclassTableBinding.getTable().getTableExpression(), equalTo( "joined_entity_leaf" ) );
		assertThat( secondSubclassTableBinding.getIdentificationVariable(), equalTo( "j1_1" ) );
	}

	@Test
	public void testSingleSpaceBranch() {
		SqmSelectStatement sqm = (SqmSelectStatement) interpret( "from JoinedEntityBranch" );

		final ImprovedEntityPersister improvedEntityPersister =
				(ImprovedEntityPersister) getConsumerContext().getDomainMetamodel().resolveEntityReference( "JoinedEntityBranch" );
		assertThat( improvedEntityPersister.getEntityPersister(), instanceOf( JoinedSubclassEntityPersister.class ) );

		// interpreter set up
		final QuerySpec querySpec = new QuerySpec();
		final SqlAliasBaseManager aliasBaseManager = new SqlAliasBaseManager();
		final FromClauseIndex fromClauseIndex = new FromClauseIndex();

		final EntityTableGroup result = improvedEntityPersister.buildTableGroup(
				sqm.getQuerySpec().getFromClause().getFromElementSpaces().get( 0 ).getRoot(),
				querySpec.getFromClause().makeTableSpace(),
				aliasBaseManager,
				fromClauseIndex
		);
		assertThat( result, notNullValue() );
		assertThat( result.getAliasBase(), equalTo( "j1" ) );

		assertThat( result.getRootTableBinding(), notNullValue() );
		assertThat( result.getRootTableBinding().getTable(), instanceOf( PhysicalTable.class ) );
		final TableBinding tableBindingSpec = result.getRootTableBinding();
		assertThat( tableBindingSpec.getTable().getTableExpression(), equalTo( "joined_entity_branch" ) );
		assertThat( tableBindingSpec.getIdentificationVariable(), equalTo( "j1" ) );

		assertThat( result.getTableJoins().size(), equalTo( 2 ) );

		assertThat(
				result.getTableJoins().get( 0 ).getJoinedTableBinding().getTable(),
				instanceOf( PhysicalTable.class )
		);
		final TableBinding firstSubclassTableBinding = result.getTableJoins().get( 0 ).getJoinedTableBinding();
		assertThat( firstSubclassTableBinding.getTable().getTableExpression(), equalTo( "joined_entity_base" ) );
		assertThat( firstSubclassTableBinding.getIdentificationVariable(), equalTo( "j1_0" ) );

		assertThat(
				result.getTableJoins().get( 1 ).getJoinedTableBinding().getTable(),
				instanceOf( PhysicalTable.class )
		);
		final TableBinding secondSubclassTableBinding = result.getTableJoins().get( 1 ).getJoinedTableBinding();
		assertThat( secondSubclassTableBinding.getTable().getTableExpression(), equalTo( "joined_entity_leaf" ) );
		assertThat( secondSubclassTableBinding.getIdentificationVariable(), equalTo( "j1_1" ) );
	}

	@Test
	public void testSingleSpaceLeaf() {
		SqmSelectStatement sqm = (SqmSelectStatement) interpret( "from JoinedEntityLeaf" );

		final ImprovedEntityPersister improvedEntityPersister =
				(ImprovedEntityPersister) getConsumerContext().getDomainMetamodel().resolveEntityReference( "JoinedEntityLeaf" );
		assertThat( improvedEntityPersister.getEntityPersister(), instanceOf( JoinedSubclassEntityPersister.class ) );

		// interpreter set up
		final QuerySpec querySpec = new QuerySpec();
		final SqlAliasBaseManager aliasBaseManager = new SqlAliasBaseManager();
		final FromClauseIndex fromClauseIndex = new FromClauseIndex();

		final EntityTableGroup result = improvedEntityPersister.buildTableGroup(
				sqm.getQuerySpec().getFromClause().getFromElementSpaces().get( 0 ).getRoot(),
				querySpec.getFromClause().makeTableSpace(),
				aliasBaseManager,
				fromClauseIndex
		);
		assertThat( result, notNullValue() );
		assertThat( result.getAliasBase(), equalTo( "j1" ) );

		assertThat( result.getRootTableBinding(), notNullValue() );
		assertThat( result.getRootTableBinding().getTable(), instanceOf( PhysicalTable.class ) );
		final TableBinding tableBindingSpec = result.getRootTableBinding();
		assertThat( tableBindingSpec.getTable().getTableExpression(), equalTo( "joined_entity_leaf" ) );
		assertThat( tableBindingSpec.getIdentificationVariable(), equalTo( "j1" ) );

		assertThat( result.getTableJoins().size(), equalTo( 2 ) );

		assertThat(
				result.getTableJoins().get( 0 ).getJoinedTableBinding().getTable(),
				instanceOf( PhysicalTable.class )
		);
		final TableBinding firstSubclassTableBinding = result.getTableJoins().get( 0 ).getJoinedTableBinding();
		assertThat( firstSubclassTableBinding.getTable().getTableExpression(), equalTo( "joined_entity_branch" ) );
		assertThat( firstSubclassTableBinding.getIdentificationVariable(), equalTo( "j1_0" ) );

		assertThat(
				result.getTableJoins().get( 1 ).getJoinedTableBinding().getTable(),
				instanceOf( PhysicalTable.class )
		);
		final TableBinding secondSubclassTableBinding = result.getTableJoins().get( 1 ).getJoinedTableBinding();
		assertThat( secondSubclassTableBinding.getTable().getTableExpression(), equalTo( "joined_entity_base" ) );
		assertThat( secondSubclassTableBinding.getIdentificationVariable(), equalTo( "j1_1" ) );
	}


	@Override
	protected void applyMetadataSources(MetadataSources metadataSources) {
		metadataSources.addAnnotatedClass( JoinedEntityBase.class );
		metadataSources.addAnnotatedClass( JoinedEntityBranch.class );
		metadataSources.addAnnotatedClass( JoinedEntityLeaf.class );
	}

	@Entity( name = "JoinedEntityBase" )
	@Table( name = "joined_entity_base" )
	@Inheritance( strategy = InheritanceType.JOINED )
	public static class JoinedEntityBase {
		@Id
		public Integer id;
		public String name;
		public String description;
	}

	@Entity( name = "JoinedEntityBranch" )
	@Table( name = "joined_entity_branch" )
	public static class JoinedEntityBranch extends JoinedEntityBase {
		public String branchSpecificState;
	}

	@Entity( name = "JoinedEntityLeaf" )
	@Table( name = "joined_entity_leaf" )
	public static class JoinedEntityLeaf extends JoinedEntityBranch {
		public String leafSpecificState;
	}
}
