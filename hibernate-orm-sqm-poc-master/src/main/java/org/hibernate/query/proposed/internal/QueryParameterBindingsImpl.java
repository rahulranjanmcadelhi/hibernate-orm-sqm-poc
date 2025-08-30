/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.proposed.internal;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.QueryException;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.query.proposed.ParameterMetadata;
import org.hibernate.query.proposed.QueryParameter;
import org.hibernate.query.proposed.spi.QueryParameterBinding;
import org.hibernate.query.proposed.spi.QueryParameterBindingTypeResolver;
import org.hibernate.query.proposed.spi.QueryParameterBindings;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public class QueryParameterBindingsImpl implements QueryParameterBindings {
	private static final Logger log = Logger.getLogger( QueryParameterBindingsImpl.class );

	/**
	 * Generates the QueryParameterBindingsImpl instance
	 */
	public static QueryParameterBindingsImpl from(
			ParameterMetadata parameterMetadata,
			QueryParameterBindingTypeResolver resolver) {
		return new QueryParameterBindingsImpl( resolver, parameterMetadata );
	}


	private final QueryParameterBindingTypeResolver resolver;
	private final ParameterMetadata parameterMetadata;

	private final Map<QueryParameter, QueryParameterBinding> bindingMap = new HashMap<>();

	private QueryParameterBindingsImpl(QueryParameterBindingTypeResolver resolver, ParameterMetadata parameterMetadata) {
		this.resolver = resolver;
		this.parameterMetadata = parameterMetadata;

		for ( QueryParameter queryParameter : parameterMetadata.collectAllParameters() ) {
			registerBinding( queryParameter, makeBinding( queryParameter ) );
		}
	}

	@SuppressWarnings("unchecked")
	private QueryParameterBinding makeBinding(QueryParameter queryParameter) {
		assert queryParameter != null;

		return new QueryParameterBindingImpl( queryParameter, resolver );
	}

	@Override
	public void validate() {
		for ( QueryParameter<?> queryParameter : parameterMetadata.collectAllParameters() ) {
			if ( !isBound( queryParameter ) ) {
				if ( queryParameter.getName() != null ) {
					throw new QueryException( "Named parameter [" + queryParameter.getName() + "] not set" );
				}
				else if ( queryParameter.getPosition() != null ) {
					throw new QueryException( "Positional parameter [" + queryParameter.getPosition() + "] not set" );
				}
				else {
					throw new QueryException( "Parameter [" + queryParameter + "] not set" );
				}
			}
		}
	}

	@Override
	public boolean isBound(QueryParameter parameter) {
		final QueryParameterBinding binding = locateBinding( parameter );
		return binding != null && binding.isBound();
	}

	@Override
	public <T> QueryParameterBinding<T> getBinding(QueryParameter<T> parameter) {
		final QueryParameterBinding<T> binding = locateBinding( parameter );

		if ( binding == null ) {
			throw new IllegalArgumentException(
					"Could not resolve QueryParameter reference [" + parameter + "] to QueryParameterBinding"
			);
		}

		return binding;
	}

	@SuppressWarnings("unchecked")
	protected  <T> QueryParameterBinding<T> locateBinding(QueryParameter<T> parameter) {
		// see if this exact instance is known as a key
		if ( bindingMap.containsKey( parameter ) ) {
			return bindingMap.get( parameter );
		}

		// if the incoming parameter has a name, try to find it by name
		if ( StringHelper.isNotEmpty( parameter.getName() ) ) {
			final QueryParameterBinding binding = locateBinding( parameter.getName() );
			if ( binding != null ) {
				return binding;
			}
		}

		// if the incoming parameter has a position, try to find it by position
		if ( parameter.getPosition() != null ) {
			final QueryParameterBinding binding = locateBinding( parameter.getPosition() );
			if ( binding != null ) {
				return binding;
			}
		}

		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> QueryParameterBinding<T> getBinding(String name) {
		final QueryParameterBinding binding = locateBinding( name );
		if ( binding == null ) {
			throw new IllegalArgumentException( "Unknown parameter name : " + name );
		}

		return binding;
	}

	@SuppressWarnings("unchecked")
	protected <T> QueryParameterBinding<T> locateBinding(String name) {
		for ( Map.Entry<QueryParameter, QueryParameterBinding> entry : bindingMap.entrySet() ) {
			if ( name.equals( entry.getKey().getName() ) ) {
				return entry.getValue();
			}
		}

		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> QueryParameterBinding<T> getBinding(int position) {
		final QueryParameterBinding binding = locateBinding( position );
		if ( binding == null ) {
			throw new IllegalArgumentException( "Unknown parameter position : " + position );
		}

		return binding;
	}

	@SuppressWarnings("unchecked")
	protected <T> QueryParameterBinding<T> locateBinding(int position) {
		for ( Map.Entry<QueryParameter, QueryParameterBinding> entry : bindingMap.entrySet() ) {
			if ( entry.getKey().getPosition() == null ) {
				continue;
			}

			if ( entry.getKey().getPosition() == position ) {
				return entry.getValue();
			}
		}

		return null;
	}


	protected void registerBinding(QueryParameter queryParameter, QueryParameterBinding binding) {
		log.debugf( "Registering parameter binding : [%s] -> [%s]", queryParameter, binding );
		bindingMap.put( queryParameter, binding );
	}

	/**
	 * NOTE : do not mutate the binding map via this return.  It is not supported,
	 * but since this is an internal only contract I decided not to wrap it as
	 * unmodifiable for performance.
	 */
	protected Map<QueryParameter, QueryParameterBinding> getBindingMap() {
		return bindingMap;
	}
}
