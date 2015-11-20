package eu.riscoss.rdc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class DuplicateMap<K, V> implements Iterable<V> {
	
	private Map<K, List<V>>		map		= new HashMap<K, List<V>>();

	private ArrayList<V>		order	= new ArrayList<V>();

	public void add( K key, V value ) {
		put( key, value );
	}

	public void clear() {
		this.map.clear();
		this.order.clear();
	}

	public boolean containsKey( K key ) {
		return this.map.containsKey( key );
	}

	public boolean containsValue( V value ) {
		return this.order.contains( value );
	}

	public int count( K key ) {
		List<V> list = this.map.get( key );

		if( list == null ) {
			return 0;
		}

		return list.size();
	}

	public V get( K key ) {
		return get( key, 0 );
	}

	public V get( K key, int index ) {
		List<V> list = this.map.get( key );

		if( list == null ) {
			return null;
		}

		if( index > (list.size() - 1) ) {
			return null;
		}

		return list.get( index );
	}

	public int getKeyCount() {
		return this.map.size();
	}

	public java.util.List<V> getList( K key ) {
		return this.map.get( key );
	}

	public boolean isEmpty() {
		return this.map.isEmpty();
	}

	public int keyCount() {
		return this.map.size();
	}

	public Set<K> keySet() {
		return this.map.keySet();
	}

	public List<V> list( K key ) {
		List<V> list = this.map.get( key );
		if( list != null ) {
			return list;
		}
		return new ArrayList<V>();
	}

	public void put( K key, V value ) {
		List<V> existing = this.map.get( key );
		if( existing == null ) {
			List<V> list = new ArrayList<V>();
			list.add( value );
			this.map.put( key, list );
		} else {
			existing.add( value );
		}
		this.order.add( value );
	}

	public void remove( K key ) {
		for( V val : list( key ) ) {
			order.remove( val );
		}
		
		map.remove( key );
	}

	public void remove( K key, int n ) {
		V o = get( key );

		List<V> list = this.map.get( key );

		if( list == null ) {
			return;
		}

		list.remove( n );

		this.order.remove( o );
	}

	public int size() {
		return this.order.size();
	}
	
	public void putAll( DuplicateMap<K,V> newmap ) {
		
		for( K key : newmap.keySet() ) {
			
			for( int v = 0; v < newmap.count( key ); v++ ) {
				
				V value = newmap.get( key, v );
				
				this.put( key, value );
			}
		}
	}
	
	class ValueIterator implements Iterator<V> {
		
		int x = 0, y = 0;
		
		@Override
		public boolean hasNext() {
			return x < order.size();
		}

		@Override
		public V next() {
			
			List<V> list = map.get( order.get( x ) );
			
			V v = list.get( y );
			
			y++;
			
			if( y >= list.size() ) {
				y = 0;
				x++;
			}
			
			return v;
		}

		@Override
		public void remove() {}
		
	}
	
	@Override
	public Iterator<V> iterator() {
		return order.iterator();
	}

	public V getValue( int i ) {
		return order.get( i );
	}
}
