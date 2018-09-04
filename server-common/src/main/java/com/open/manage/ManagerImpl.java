package com.open.manage;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class ManagerImpl<K, T extends IResourceWithId<K>> implements IManager <K, T> {
	protected Map<K, T> idToObjectMap = new HashMap<K, T>();
	protected Set<T> objectSet = new HashSet<T>();

	public abstract Map<K, T> load() throws Exception;

	ReadWriteLock lock;
	 
	
	public void init() {
		try {

			lock = new ReentrantReadWriteLock(false);
			reload();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Set<T> getAll() {
		return this.objectSet;
	}

	public Object getLock() {
		return this.idToObjectMap;
	}

	public void add(K key, T object) {
		synchronized (this.idToObjectMap) {
			preAdd(object);
			this.idToObjectMap.put(key, object);
			this.objectSet.add(object);
			postAdd(object);
		}
	}

	public void replace(K key, T object) {
		synchronized (this.idToObjectMap) {
			T old = this.idToObjectMap.get(key);
			preReplace(old, object);
			if (old == null) {
				this.idToObjectMap.put(key, object);
				postReplace(old, object);
				return;
			}
			Field[] fields = old.getClass().getDeclaredFields();
			for (Field field : fields) {
				if (Modifier.isStatic(field.getModifiers()))
					continue;
				try {
					field.setAccessible(true);
					Object value = field.get(object);

					if (value != null) {
						field.set(old, value);
					}
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}

			this.objectSet.add(object);

			postReplace(old, object);
		}
	}

	public void deleteById(K key) {
		synchronized (this.idToObjectMap) {
			T old = this.idToObjectMap.get(key);
			if (old != null) {
				preDelete(old);
				this.objectSet.remove(old);
				this.idToObjectMap.remove(key);
				postDelete(old);
			}
		}
	}

	public T getByIdWidthKey(K key) {
		synchronized (this.idToObjectMap) {
			return this.idToObjectMap.get(key);
		}
	}

	public T getById(K key) {

		return this.idToObjectMap.get(key);

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public synchronized void reload() throws Exception {
		Map newMap = load();
		System.err.println("[Manager][" + getClass().getSimpleName() + "] loaded " + newMap.values().size());
		Set newSet = new HashSet();
		newSet.addAll(newMap.values());
		synchronized (this.idToObjectMap) {
			preReload();
			this.objectSet = newSet;
			this.idToObjectMap = newMap;
			postReload();
		}
	}

	public Map<K, T> getIdToObjectMap() {
		return this.idToObjectMap;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <K, T extends IResourceWithId> Map<?, ?> fromCollection(
			Collection<? extends IResourceWithId> collection) {
		Map map = new ConcurrentHashMap();
		if (collection != null) {
			for (IResourceWithId obj : collection) {
				map.put(obj.getResourceId(), obj);
			}
		}
		return map;
	}

	public void preReload() {
	}

	public void postReload() {
	}

	public void postDelete(IResourceWithId old) {
	}

	public void preDelete(T oldObj) {
	}

	public void preAdd(T newObj) {
	}

	public void postAdd(T newObj) {
	}

	public void preReplace(T oldObj, T newObj) {
	}

	public void postReplace(T oldObj, T newObj) {
	}
}
