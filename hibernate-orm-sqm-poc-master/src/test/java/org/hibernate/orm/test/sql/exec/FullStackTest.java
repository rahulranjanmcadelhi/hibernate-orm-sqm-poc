/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.test.sql.exec;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Tuple;

import org.hibernate.Session;
import org.hibernate.boot.MetadataSources;
import org.hibernate.orm.test.sql.BaseExecutionTest;
import org.hibernate.query.proposed.internal.sqm.QuerySqmImpl;

import org.junit.Test;

import org.hamcrest.CoreMatchers;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Steve Ebersole
 */
public class FullStackTest extends BaseExecutionTest {
	@Override
	public void before() throws Exception {
		super.before();
		insertRow();
	}

	@Override
	protected void applyMetadataSources(MetadataSources metadataSources) {
		super.applyMetadataSources( metadataSources );
		metadataSources.addAnnotatedClass( Person.class );
		metadataSources.addAnnotatedClass( Address.class );
	}

	private void insertRow() {
		Session session = getSessionFactory().openSession();
		session.beginTransaction();
		Address addr = new Address( 1, "123 Main", "Anywhere, USA" );
		session.persist( addr );
		session.persist( new Person( 1, "Steve", 20, addr ) );
		session.getTransaction().commit();
		session.close();
	}

	@Test
	public void testFullStackBasicAttributeSelection() throws SQLException {
		doInSession(
			session -> {
				final QuerySqmImpl query = generateQueryImpl(
						session,
						"select p.name from Person p where p.age >= 20 and p.age <= ?1",
						null
				);

				query.setParameter( 1, 39 );
				final List results = query.list();

				assertThat( results.size(), is( 1 ) );
				Object row = results.get( 0 );
				assertThat( row, instanceOf( String.class ) );
				assertThat( row, is("Steve") );
			}
		);
	}

	@Test
	public void testFullStackTypedBasicAttributeSelection() throws SQLException {
		doInSession(
				session -> {
					final QuerySqmImpl<String> query = generateQueryImpl(
							session,
							"select p.name from Person p where p.age >= 20 and p.age <= ?1",
							String.class
					);

					query.setParameter( 1, 39 );
					final List results = query.list();

					assertThat( results.size(), is( 1 ) );
					assertThat( results.get( 0 ), instanceOf( String.class ) );
					String name = (String) results.get( 0 );
					assertThat( name, is("Steve") );
				}
		);
	}

	@Test
	public void testFullStackTupleTyped() throws SQLException {
		doInSession(
				session -> {
					final QuerySqmImpl<Tuple> query = generateQueryImpl(
							session,
							"select p.name as name from Person p where p.age >= 20 and p.age <= ?1",
							Tuple.class
					);

					query.setParameter( 1, 39 );
					final List<Tuple> results = query.list();
					assertThat( results.size(), is( 1 ) );
					Tuple tuple = results.get( 0 );
					assertThat( tuple.get( "name" ), is( "Steve") );
				}
		);
	}

	@Test
	public void testFullStackDynamicInstantiation() throws SQLException {
		doInSession(
				session -> {
					final QuerySqmImpl<Person> query = generateQueryImpl(
							session,
							"select new Person( p.id, p.name, p.age ) from Person p where p.age >= 20 and p.age <= ?1",
							Person.class
					);

					query.setParameter( 1, 39 );
					final List<Person> results = query.list();
					assertThat( results.size(), is( 1 ) );
					Person person = results.get( 0 );
					assertThat( person.name, is("Steve") );
					assertThat( person.id, is(1) );
					assertThat( person.age, is(20) );
				}
		);
	}

	@Test
	public void testFullStackDynamicInstantiationInjection() throws SQLException {
		doInSession(
				session -> {
					final QuerySqmImpl<Person> query = generateQueryImpl(
							session,
							"select new Person( p.id, p.name as name, p.age as age ) from Person p where p.age >= 20 and p.age <= ?1",
							Person.class
					);

					query.setParameter( 1, 39 );
					final List<Person> results = query.list();
					assertThat( results.size(), is( 1 ) );
					Person person = results.get( 0 );
					assertThat( person.id, is(1) );
					assertThat( person.name, is("Steve") );
					assertThat( person.age, is(20) );
				}
		);
	}

	@Test
	public void testFullStackDynamicInstantiationList() throws SQLException {
		doInSession(
				session -> {
					final QuerySqmImpl<List> query = generateQueryImpl(
							session,
							"select new list(p.name, p.age) from Person p where p.age >= 20 and p.age <= ?1",
							List.class
					);

					query.setParameter( 1, 39 );

					final List<List> results = query.list();
					assertThat( results.size(), is( 1 ) );
					List tuples = results.get( 0 );
					assertThat( tuples.size(), is(2) );
					assertThat( tuples.get(0), CoreMatchers.is( "Steve") );
					assertThat( tuples.get(1), CoreMatchers.is( 20) );
				}
		);
	}

	@Test
	public void testFullStackDynamicInstantiationMap() throws SQLException {
		doInSession(
				session -> {
					final QuerySqmImpl<Map> query = generateQueryImpl(
							session,
							"select new map(p.name as name, p.age as age) from Person p where p.age >= 20 and p.age <= ?1",
							Map.class
					);

					query.setParameter( 1, 39 );

					final List<Map> results = query.list();
					assertThat( results.size(), is( 1 ) );
					Map tuples = results.get( 0 );
					assertThat( tuples.size(), is(2) );
					assertThat( tuples.get("name"), CoreMatchers.is( "Steve") );
					assertThat( tuples.get("age"), CoreMatchers.is( 20) );
				}
		);
	}

	@Test
	public void testFullStackManyToAttributeSelection() throws SQLException {
		doInSession(
				session -> {
					final QuerySqmImpl<Map> query = generateQueryImpl(
							session,
							"select p.address from Person p where p.age >= 20 and p.age <= ?1",
							Map.class
					);

					query.setParameter( 1, 39 );

					final List results = query.list();
					assertThat( results.size(), is( 1 ) );
					Address address = (Address) results.get( 0 );
					assertThat( address.id, is(1) );
					assertThat( address.streetAddress, CoreMatchers.is( "123 Main" ) );
					assertThat( address.city, CoreMatchers.is( "Anywhere, USA" ) );
				}
		);
	}

	@Entity(name="Person")
	@SuppressWarnings({"WeakerAccess", "unused"})
	public static class Person {
		@Id
		Integer id;
		String name;
		int age;
		@ManyToOne( cascade = CascadeType.ALL )
		Address address;

		public Person() {
		}

		public Person(Integer id, String name, int age) {
			this.id = id;
			this.name = name;
			this.age = age;
		}

		public Person(Integer id, String name, int age, Address address) {
			this.id = id;
			this.name = name;
			this.age = age;
			this.address = address;
		}
	}

	@Entity(name="Address")
	@SuppressWarnings({"WeakerAccess", "unused"})
	public static class Address {
		@Id
		Integer id;
		String streetAddress;
		String city;

		public Address() {
		}

		public Address(Integer id, String streetAddress, String city) {
			this.id = id;
			this.streetAddress = streetAddress;
			this.city = city;
		}
	}
}
