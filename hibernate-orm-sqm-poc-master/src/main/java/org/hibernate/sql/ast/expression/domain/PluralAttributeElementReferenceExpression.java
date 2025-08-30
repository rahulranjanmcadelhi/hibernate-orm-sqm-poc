/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.expression.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.loader.PropertyPath;
import org.hibernate.persister.collection.internal.PluralAttributeElementEntity;
import org.hibernate.persister.collection.spi.ImprovedCollectionPersister;
import org.hibernate.persister.collection.spi.PluralAttributeElement;
import org.hibernate.persister.common.spi.Column;
import org.hibernate.sql.NotYetImplementedException;
import org.hibernate.sql.ast.from.ColumnBinding;
import org.hibernate.sql.ast.from.TableGroup;
import org.hibernate.sql.ast.select.Selectable;
import org.hibernate.sql.ast.select.SelectableBasicTypeImpl;
import org.hibernate.sql.ast.select.SelectableEmbeddedTypeImpl;
import org.hibernate.sql.ast.select.SelectableEntityTypeImpl;
import org.hibernate.sql.exec.spi.SqlAstSelectInterpreter;
import org.hibernate.type.BasicType;
import org.hibernate.type.CompositeType;
import org.hibernate.type.Type;

/**
 * @author Steve Ebersole
 */
public class PluralAttributeElementReferenceExpression implements DomainReferenceExpression {
	private final ImprovedCollectionPersister collectionPersister;
	private final ColumnBindingSource columnBindingSource;
	private final PropertyPath propertyPath;

	private final Selectable selectable;
	private final List<ColumnBinding> columnBindings;

	public PluralAttributeElementReferenceExpression(
			ImprovedCollectionPersister collectionPersister,
			TableGroup columnBindingSource,
			PropertyPath propertyPath,
			boolean isShallow) {
		this.collectionPersister = collectionPersister;
		this.columnBindingSource = columnBindingSource;
		this.propertyPath = propertyPath;

		// todo : why are these casts to Column needed? elementReference.getColumns() returns List<Column>

		final PluralAttributeElement elementReference = collectionPersister.getElementReference();
		switch ( elementReference.getClassification() ) {
			case BASIC: {
				final Column column = (Column) elementReference.getColumns().get( 0 );
				final ColumnBinding columnBinding = columnBindingSource.resolveColumnBinding( column );
				this.columnBindings = Collections.singletonList( columnBinding );
				this.selectable = new SelectableBasicTypeImpl(
						this,
						columnBinding,
						(BasicType) elementReference.getOrmType()
				);
				break;
			}
			case EMBEDDABLE: {
				this.columnBindings = new ArrayList<>();
				for ( Object ugh : elementReference.getColumns() ) {
					final Column column = (Column) ugh;
					this.columnBindings.add( columnBindingSource.resolveColumnBinding( column ) );
				}
				this.selectable = new SelectableEmbeddedTypeImpl(
						this,
						columnBindings,
						(CompositeType) elementReference
				);
				break;
			}
			case ONE_TO_MANY:
			case MANY_TO_MANY: {
				final PluralAttributeElementEntity entityElement = (PluralAttributeElementEntity) collectionPersister.getElementReference();
				this.columnBindings = new ArrayList<>();
				for ( Column column : entityElement.getColumns() ) {
					this.columnBindings.add( columnBindingSource.resolveColumnBinding( column ) );
				}
				this.selectable = new SelectableEntityTypeImpl(
						this,
						getPropertyPath(),
						columnBindingSource,
						( ( PluralAttributeElementEntity) collectionPersister.getElementReference() ).getElementPersister(),
						isShallow
				);
				break;
			}
			default: {
				throw new NotYetImplementedException(
						"Resolution of Selectable for plural-attribute elements of classification [" +
								elementReference.getClassification().name() +
								"] not yet implemented"
				);
			}
		}
	}

	@Override
	public Type getType() {
		return null;
	}

	@Override
	public Selectable getSelectable() {
		return selectable;
	}

	@Override
	public void accept(SqlAstSelectInterpreter walker) {
		walker.visitPluralAttributeElement( this );
	}

	@Override
	public PropertyPath getPropertyPath() {
		return propertyPath;
	}

	@Override
	public List<ColumnBinding> getColumnBindings() {
		return columnBindings;
	}

	@Override
	public PluralAttributeElement getDomainReference() {
		return collectionPersister.getElementReference();
	}


}
