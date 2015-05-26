package uk.nhs.ciao.spring;

import org.springframework.beans.factory.FactoryBean;

/**
 * Utility factory bean which always returns a pre-configured
 * instance.
 *
 * @param <T> The type of object provided by this factory
 */
public class SingletonFactoryBean<T> implements FactoryBean<T> {
	private final Class<T> objectType;
	private final T object;
	
	/**
	 * Constructs a new factory bean to supply the specified instance
	 */
	public SingletonFactoryBean(final Class<T> objectType, final T object) {
		this.objectType = objectType;
		this.object = object;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isSingleton() {
		return true;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public T getObject() {
		return object;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?> getObjectType() {
		return objectType;
	}
}
