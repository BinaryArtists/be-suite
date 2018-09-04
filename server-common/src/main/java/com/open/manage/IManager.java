package com.open.manage;


import java.util.Map;
import java.util.Set;

public interface IManager<K, T extends IResourceWithId<K>> {
	public void init();
	
	public Set<T> getAll();

	public Object getLock();

	public void add(K key, T object) ;

	public void replace(K key, T object) ;
	public void deleteById(K key);

	public T getByIdWidthKey(K key);

	public T getById(K key);

	


	public Map<K, T> getIdToObjectMap();

	
	

	public void preReload();

	public void postReload();

	public void postDelete(IResourceWithId<K> old);
	

	public void preDelete(T oldObj);

	public void preAdd(T newObj);

	public void postAdd(T newObj);

	public void preReplace(T oldObj, T newObj);

	public void postReplace(T oldObj, T newObj);
}
