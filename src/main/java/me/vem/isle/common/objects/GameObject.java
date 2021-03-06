package me.vem.isle.common.objects;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;

//TODO Separate Common from Client
import me.vem.isle.client.resources.Sprite;

import me.vem.isle.common.Game;
import me.vem.isle.common.RIdentifiable;
import me.vem.isle.common.controller.Controller;
import me.vem.isle.common.physics.Physics;
import me.vem.isle.common.physics.collider.Collider;
import me.vem.isle.common.world.Chunk;
import me.vem.isle.common.world.World;
import me.vem.utils.io.Compressable;
import me.vem.utils.math.Vector;

public class GameObject implements Comparable<GameObject>, Compressable, RIdentifiable{
	
	public static Queue<GameObject> toDestroy = new LinkedList<>();
	public static boolean destroy(GameObject go) {
		return toDestroy.add(go);
	}
	
	public static void destroyQueue() {
		while(!toDestroy.isEmpty()) {
			GameObject go = toDestroy.poll();
			go.chunk.remove(go);
			if(go.isChunkLoader())
				go.chunk.unload(go.chunkRadius());
		}
	}

	protected final Property prop;
	private int RUID; //Effectively Final
	
	protected Chunk chunk;
	protected Vector pos;
	
	protected Physics physics;	
	protected Collider collider;
	protected Controller controller;
	
	public GameObject(ByteBuffer buf) {
		this(buf.getInt(), buf.getFloat(), buf.getFloat());
	}
	
	public GameObject(String id, int x, int y) {
		this(id, x+.5f, y+.5f);
	}
	
	public GameObject(String id, float x, float y) {
		this(id.hashCode(), x, y);
	}
	
	public GameObject(int hash, float x, float y) {
		this(hash, x, y, null);
	}
	
	public GameObject(String s, float x, float y, Chunk c) {
		this(s.hashCode(), x, y, c);
	}
	
	public GameObject(int hash, float x, float y, Chunk chunk) {
		Game.requestRUID(this);
		prop = Property.get(hash);
		pos = new Vector(x, y);
		
		physics = prop.buildPhysics(this);
		collider = prop.buildCollider(this);
		controller = prop.buildController(this);
		
		if(chunk == null)
			chunk = getPresumedChunk();
		
		//Chunk handling
		chunk.add(this);
		
		if(isChunkLoader())
			chunk.load(chunkRadius());
		
		if("ent_player".hashCode() == hash)
			Game.setPlayer(this);
	}
	
	
	public String getId() { return prop.getId(); }
	public int getRUID() { return RUID; }
	public boolean isId(String s) {
		return s.hashCode() == prop.hashCode();
	}
	
	public Vector getPos() { return pos; }
	public void setPos(float x, float y) { pos.set(x, y); }
	public void move(float dx, float dy) { pos.offset(dx, dy); }
	
	public float getX() { return pos.getX(); }
	public float getY() { return pos.getY(); }
	public float getZ() { return prop.getZ(); }
	
	public Physics getPhysics() { return physics; }
	public Collider getCollider() { return collider; }
	
	public Chunk getAssignedChunk() { return chunk; }
	public Chunk getPresumedChunk() { return World.getInstance().getChunkFor(pos); }
	
	public boolean hasPhysics() { return physics != null; }
	public boolean hasCollider() { return collider != null; }
	
	public boolean isChunkLoader() { return prop.isChunkLoader(); }
	public int chunkRadius() { return prop.getLoadRadius(); }
	
	public Sprite getSprite() { return prop.getSprite(); }
	
	public boolean collidedWith(GameObject go) {
		return collider.collidedWith(go.collider);
	}
	
	public void setChunk(Chunk c) { this.chunk = c; }
	
	public void update(float dt) {
		if(hasPhysics()) 
			physics.update(dt);
		
		if(controller != null)
			controller.update(dt);
	}

	@Override
	public int compareTo(GameObject o) {
		if(getZ() == o.getZ())
			return RUID - o.RUID;
		
		return (int)Math.signum(getZ() - o.getZ());
	}
	
	@Override
	public String toString() {
		return String.format("GameObject[%s:%s]", prop.getId(), pos);
	}
	
	@Override
	public synchronized ByteBuffer writeTo(ByteBuffer buf) {
		buf.putInt(prop.hashCode());
		pos.writeTo(buf);
		
		return buf;
	}
	
	@Override public int writeSize() { return 12; }

	@Override
	public boolean setRUID(int RUID) {
		if(this.getRUID() > 0) return false;
		
		this.RUID = RUID;
		return true;
	}
}
