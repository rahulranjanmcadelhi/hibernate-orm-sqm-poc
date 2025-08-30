/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.persister.common.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.collection.internal.ImprovedCollectionPersisterImpl;
import org.hibernate.persister.common.spi.AbstractAttribute;
import org.hibernate.persister.common.spi.AttributeContainer;
import org.hibernate.persister.common.spi.Column;
import org.hibernate.persister.common.spi.JoinableAttributeContainer;
import org.hibernate.persister.common.spi.Table;
import org.hibernate.persister.embeddable.EmbeddablePersister;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.entity.OuterJoinLoadable;
import org.hibernate.persister.entity.spi.ImprovedEntityPersister;
import org.hibernate.sql.NotYetImplementedException;
import org.hibernate.sqm.domain.PluralAttributeElementReference.ElementClassification;
import org.hibernate.sqm.domain.PluralAttributeReference.CollectionClassification;
import org.hibernate.sqm.domain.SingularAttributeReference.SingularAttributeClassification;
import org.hibernate.sqm.query.PropertyPath;
import org.hibernate.type.ArrayType;
import org.hibernate.type.BagType;
import org.hibernate.type.BasicType;
import org.hibernate.type.CollectionType;
import org.hibernate.type.CompositeType;
import org.hibernate.type.IdentifierBagType;
import org.hibernate.type.ListType;
import org.hibernate.type.MapType;
import org.hibernate.type.OrderedMapType;
import org.hibernate.type.OrderedSetType;
import org.hibernate.type.SetType;
import org.hibernate.type.SortedMapType;
import org.hibernate.type.SortedSetType;
import org.hibernate.type.Type;

/**
 * For now mainly a helper for reflection into stuff not exposed on the entity/collection persister
 * contracts
 *
 * @author Steve Ebersole
 */
public class PersisterHelper {
	private final Method subclassTableSpanMethod;
	private final Method subclassPropertyTableNumberMethod;
	private final Method subclassPropertyColumnsMethod;
	private final Method subclassPropertyFormulasMethod;

	/**
	 * Singleton access
	 */
	public static final PersisterHelper INSTANCE = new PersisterHelper();

	private PersisterHelper() {
		try {
			subclassTableSpanMethod = AbstractEntityPersister.class.getDeclaredMethod( "getSubclassTableSpan" );
			subclassTableSpanMethod.setAccessible( true );

			subclassPropertyTableNumberMethod = AbstractEntityPersister.class.getDeclaredMethod( "getSubclassPropertyTableNumber", int.class );
			subclassPropertyTableNumberMethod.setAccessible( true );

			subclassPropertyColumnsMethod = AbstractEntityPersister.class.getDeclaredMethod( "getSubclassPropertyColumnReaderClosure" );
			subclassPropertyColumnsMethod.setAccessible( true );

			subclassPropertyFormulasMethod = AbstractEntityPersister.class.getDeclaredMethod( "getSubclassPropertyFormulaTemplateClosure" );
			subclassPropertyFormulasMethod.setAccessible( true );
		}
		catch (Exception e) {
			throw new HibernateException( "Unable to initialize access to AbstractEntityPersister#getSubclassTableSpan", e );
		}
	}

	public int extractSubclassTableCount(EntityPersister persister) {
		try {
			return (Integer) subclassTableSpanMethod.invoke( persister );
		}
		catch (InvocationTargetException e) {
			throw new HibernateException(
					"Unable to access AbstractEntityPersister#getSubclassTableSpan [" + persister.toString() + "]",
					e.getTargetException()
			);
		}
		catch (Exception e) {
			throw new HibernateException(
					"Unable to access AbstractEntityPersister#getSubclassTableSpan [" + persister.toString() + "]",
					e
			);
		}
	}

	public int getSubclassPropertyTableNumber(EntityPersister persister, int subclassPropertyNumber) {
		try {
			return (Integer) subclassPropertyTableNumberMethod.invoke( persister, subclassPropertyNumber );
		}
		catch (InvocationTargetException e) {
			throw new HibernateException(
					"Unable to access AbstractEntityPersister#getSubclassPropertyTableNumber [" + persister.toString() + "]",
					e.getTargetException()
			);
		}
		catch (Exception e) {
			throw new HibernateException(
					"Unable to access AbstractEntityPersister#getSubclassPropertyTableNumber [" + persister.toString() + "]",
					e
			);
		}
	}

	public Table getPropertyTable(EntityPersister persister, String attributeName, Table[] tables) {
		final String tableName = ( ( OuterJoinLoadable) persister ).getPropertyTableName( attributeName );
		for ( Table table : tables ) {
			if ( table instanceof UnionSubclassTable ) {
				if ( ( (UnionSubclassTable) table ).includes( tableName ) ) {
					return table;
				}
			}
			if ( table.getTableExpression().equals( tableName ) ) {
				return table;
			}
		}
		throw new HibernateException(
				"Could not locate Table for attribute [" + persister.getEntityName() + ".'" + attributeName + "]"
		);
	}

	public String[] getSubclassPropertyColumnExpressions(EntityPersister persister, int subclassPropertyNumber) {
		try {
			final String[][] columnExpressions = (String[][]) subclassPropertyColumnsMethod.invoke( persister );
			return columnExpressions[subclassPropertyNumber];
		}
		catch (InvocationTargetException e) {
			throw new HibernateException(
					"Unable to access AbstractEntityPersister#getSubclassPropertyTableNumber [" + persister.toString() + "]",
					e.getTargetException()
			);
		}
		catch (Exception e) {
			throw new HibernateException(
					"Unable to access AbstractEntityPersister#getSubclassPropertyTableNumber [" + persister.toString() + "]",
					e
			);
		}
	}

	public String[] getSubclassPropertyFormulaExpressions(EntityPersister persister, int subclassPropertyNumber) {
		try {
			final String[][] columnExpressions = (String[][]) subclassPropertyFormulasMethod.invoke( persister );
			return columnExpressions[subclassPropertyNumber];
		}
		catch (InvocationTargetException e) {
			throw new HibernateException(
					"Unable to access AbstractEntityPersister#getSubclassPropertyTableNumber [" + persister.toString() + "]",
					e.getTargetException()
			);
		}
		catch (Exception e) {
			throw new HibernateException(
					"Unable to access AbstractEntityPersister#getSubclassPropertyTableNumber [" + persister.toString() + "]",
					e
			);
		}
	}

	public static List<Column> makeValues(
			SessionFactoryImplementor factory,
			Type type,
			String[] columns,
			String[] formulas,
			Table table) {
		assert formulas == null || columns.length == formulas.length;

		final List<Column> values = new ArrayList<>();

		for ( int i = 0; i < columns.length; i++ ) {
			final int jdbcType = type.sqlTypes( factory )[i];

			if ( columns[i] != null ) {
				values.add( table.makeColumn( columns[i], jdbcType ) );
			}
			else {
				if ( formulas == null ) {
					throw new IllegalStateException( "Column name was null and no formula information was supplied" );
				}
				values.add( table.makeFormula( formulas[i], jdbcType ) );
			}
		}

		return values;
	}

	public AbstractAttribute buildAttribute(
			DatabaseModel databaseModel,
			DomainMetamodelImpl domainMetamodel,
			AttributeContainer source,
			String propertyName,
			Type propertyType,
			List<Column> columns) {
		if ( propertyType.isCollectionType() ) {
			assert columns == null || columns.isEmpty();

			return buildPluralAttribute(
					databaseModel,
					domainMetamodel,
					source,
					propertyName,
					propertyType
			);
		}
		else {
			return buildSingularAttribute(
					databaseModel,
					domainMetamodel,
					source,
					propertyName,
					propertyType,
					columns
			);
		}
	}

	public AbstractAttribute buildSingularAttribute(
			DatabaseModel databaseModel,
			DomainMetamodelImpl domainMetamodel,
			AttributeContainer source,
			String attributeName,
			Type attributeType,
			List<Column> columns) {
		final SingularAttributeClassification classification = interpretSingularAttributeClassification( attributeType );
		if ( classification == SingularAttributeClassification.ANY ) {
			throw new NotYetImplementedException();
		}
		else if ( classification == SingularAttributeClassification.EMBEDDED ) {
			return new SingularAttributeEmbedded(
					source,
					(CompositeContainer) source,
					attributeName,
					buildEmbeddablePersister(
							databaseModel,
							domainMetamodel,
							(CompositeContainer) source,
							source.asLoggableText() + '.' + attributeName,
							(CompositeType) attributeType,
							columns
					)
			);
		}
		else if ( classification == SingularAttributeClassification.BASIC ) {
			return new SingularAttributeBasic(
					source,
					attributeName,
					(org.hibernate.type.BasicType) attributeType,
					columns
			);
		}
		else {
			final org.hibernate.type.EntityType ormEntityType = (org.hibernate.type.EntityType) attributeType;
			if ( ormEntityType.isOneToOne() ) {
				// the Classification here should be ONE_TO_ONE which could represent either a real PK one-to-one
				//		or a unique-FK one-to-one (logical).  If this is a real one-to-one then we should have
				//		no columns passed here and should instead use the LHS (source) PK column(s)
				assert columns == null || columns.size() == 0;
				columns = ( (ImprovedEntityPersister) source ).getIdentifierDescriptor().getColumns();
			}
			assert columns != null && columns.size() > 0;

			return new SingularAttributeEntity(
					(JoinableAttributeContainer) source,
					attributeName,
					classification,
					ormEntityType,
					(ImprovedEntityPersister) domainMetamodel.resolveEntityReference( ormEntityType.getAssociatedEntityName( domainMetamodel.getSessionFactory() ) ),
					columns
			);
		}
	}

	public EmbeddablePersister buildEmbeddablePersister(
			DatabaseModel databaseModel,
			DomainMetamodelImpl domainMetamodel,
			CompositeContainer compositeContainer,
			String role,
			CompositeType compositeType,
			List<Column> columns) {
		return new EmbeddablePersister(
				compositeContainer,
				extractEmbeddableName( compositeType ),
				role,
				compositeType,
				databaseModel,
				domainMetamodel,
				columns
		);
	}

	private static String extractEmbeddableName(org.hibernate.type.Type attributeType) {
		// todo : fixme
		return attributeType.getName();
	}

	public AbstractAttribute buildPluralAttribute(
			DatabaseModel databaseModel,
			DomainMetamodelImpl domainMetamodel,
			AttributeContainer source,
			String subclassPropertyName,
			Type attributeType) {
		final CollectionType collectionType = (CollectionType) attributeType;
		final CollectionPersister collectionPersister = domainMetamodel.getSessionFactory().getMetamodel().collectionPersister( collectionType.getRole() );

		final ImprovedCollectionPersisterImpl persister = new ImprovedCollectionPersisterImpl(
				(JoinableAttributeContainer) source,
				subclassPropertyName,
				collectionPersister
		);

		domainMetamodel.registerCollectionPersister( persister );
		return persister;
	}

	public static org.hibernate.loader.PropertyPath convert(PropertyPath propertyPath) {
		if ( propertyPath.getParent() == null ) {
			return new org.hibernate.loader.PropertyPath( null, propertyPath.getLocalPath() );
		}
		org.hibernate.loader.PropertyPath parent = convert( propertyPath.getParent() );
		return parent.append( propertyPath.getLocalPath() );
	}

	public static interface CollectionMetadata {
		CollectionClassification getCollectionClassification();
		ElementClassification getElementClassification();

		Type getForeignKeyType();
		BasicType getCollectionIdType();
		Type getElementType();
		Type getIndexType();
	}

	public static class CollectionMetadataImpl implements CollectionMetadata {
		private final CollectionClassification collectionClassification;
		private final ElementClassification elementClassification;
		private final Type foreignKeyType;
		private final BasicType collectionIdType;
		private final Type elementType;
		private final Type indexType;

		public CollectionMetadataImpl(
				CollectionClassification collectionClassification,
				ElementClassification elementClassification,
				Type foreignKeyType,
				BasicType collectionIdType,
				Type elementType,
				Type indexType) {
			this.collectionClassification = collectionClassification;
			this.elementClassification = elementClassification;
			this.foreignKeyType = foreignKeyType;
			this.collectionIdType = collectionIdType;
			this.elementType = elementType;
			this.indexType = indexType;
		}

		@Override
		public CollectionClassification getCollectionClassification() {
			return collectionClassification;
		}

		@Override
		public ElementClassification getElementClassification() {
			return elementClassification;
		}

		@Override
		public Type getForeignKeyType() {
			return foreignKeyType;
		}

		@Override
		public BasicType getCollectionIdType() {
			return collectionIdType;
		}

		@Override
		public Type getElementType() {
			return elementType;
		}

		@Override
		public Type getIndexType() {
			return indexType;
		}
	}

	public static CollectionMetadata interpretCollectionMetadata(SessionFactoryImplementor factory, CollectionType collectionType) {
		final CollectionPersister collectionPersister = factory.getMetamodel().collectionPersister( collectionType.getRole() );

		return new CollectionMetadataImpl(
				interpretCollectionClassification( collectionType ),
				interpretElementClassification( collectionPersister ),
				collectionPersister.getKeyType(),
				(BasicType) collectionPersister.getIdentifierType(),
				collectionPersister.getElementType(),
				collectionPersister.getIndexType()
		);
	}

	public static CollectionClassification interpretCollectionClassification(CollectionType collectionType) {
		if ( collectionType instanceof BagType
				|| collectionType instanceof IdentifierBagType ) {
			return CollectionClassification.BAG;
		}
		else if ( collectionType instanceof ListType
				|| collectionType instanceof ArrayType ) {
			return CollectionClassification.LIST;
		}
		else if ( collectionType instanceof SetType
				|| collectionType instanceof OrderedSetType
				|| collectionType instanceof SortedSetType ) {
			return CollectionClassification.SET;
		}
		else if ( collectionType instanceof MapType
				|| collectionType instanceof OrderedMapType
				|| collectionType instanceof SortedMapType ) {
			return CollectionClassification.MAP;
		}
		else {
			final Class javaType = collectionType.getReturnedClass();
			if ( Set.class.isAssignableFrom( javaType ) ) {
				return CollectionClassification.SET;
			}
			else if ( Map.class.isAssignableFrom( javaType ) ) {
				return CollectionClassification.MAP;
			}
			else if ( List.class.isAssignableFrom( javaType ) ) {
				return CollectionClassification.LIST;
			}

			return CollectionClassification.BAG;
		}
	}

	private static ElementClassification interpretElementClassification(CollectionPersister collectionPersister) {
		final Type elementType = collectionPersister.getElementType();

		if ( elementType.isAnyType() ) {
			return ElementClassification.ANY;
		}
		else if ( elementType.isComponentType() ) {
			return ElementClassification.EMBEDDABLE;
		}
		else if ( elementType.isEntityType() ) {
			if ( collectionPersister.isManyToMany() ) {
				return ElementClassification.MANY_TO_MANY;
			}
			else {
				return ElementClassification.ONE_TO_MANY;
			}
		}
		else {
			return ElementClassification.BASIC;
		}
	}

	public static SingularAttributeClassification interpretSingularAttributeClassification(Type attributeType) {
		assert !attributeType.isCollectionType();

		if ( attributeType.isAnyType() ) {
			return SingularAttributeClassification.ANY;
		}
		else if ( attributeType.isEntityType() ) {
			final org.hibernate.type.EntityType ormEntityType = (org.hibernate.type.EntityType) attributeType;
			return ormEntityType.isOneToOne() || ormEntityType.isLogicalOneToOne()
					? SingularAttributeClassification.ONE_TO_ONE
					: SingularAttributeClassification.MANY_TO_ONE;
		}
		else if ( attributeType.isComponentType() ) {
			return SingularAttributeClassification.EMBEDDED;
		}
		else {
			return SingularAttributeClassification.BASIC;
		}
	}

	public static SingularAttributeClassification interpretIdentifierClassification(Type ormIdType) {
		return ormIdType instanceof CompositeType
				? SingularAttributeClassification.EMBEDDED
				: SingularAttributeClassification.BASIC;
	}
}
