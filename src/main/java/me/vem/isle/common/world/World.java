package me.vem.isle.common.world;

import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

import gustavson.simplex.SimplexNoise;
import me.vem.isle.common.objects.GameObject;
import me.vem.utils.io.Compressable;
import me.vem.utils.logging.Logger;
import me.vem.utils.math.Vector;

public class World implements Compressable{

	public static World loadFrom(File worldFile) {
		try {
			if(!worldFile.exists())
				return null;
			
			FileInputStream fis = new FileInputStream(worldFile);
			
			byte[] read = new byte[(int) worldFile.length()];
			fis.read(read);
			
			fis.close();

			ByteBuffer buf = ByteBuffer.wrap(read);
			World world = new World(buf);
			Logger.info("World file loaded properly.");
			
			return world;
		}catch(IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private final int seed;
	private SimplexNoise noiseGen;
	private HashMap<Point, Chunk> chunks;
	
	public World(int seed) {
		this.seed = seed;
		chunks = new HashMap<>();
		noiseGen = new SimplexNoise(300, .5, seed);
	}
	
	private World(ByteBuffer buf) {
		this(buf.getInt());
		
		int cSize = buf.getInt();
		for(int i=0;i<cSize;i++) {
			Chunk c = new Chunk(this, buf);
			chunks.put(new Point(c.cx(), c.cy()), c);
		}
		
		int goSize = buf.getInt();
		for(int i=0;i<goSize;i++)
			new GameObject(this, buf);
	}
	
	public int getSeed() { return seed; }
	
	/**
	 * @param x
	 * @param y
	 * @return
	 */
	public Land getLand(int x, int y) {
		return getChunk(x>>4, y>>4).getLand(x&15, y&15);
	}
	
	public Land getLand(Vector pos) {
		return getLand(pos.floorX(), pos.floorY());
	}
	
	public Chunk getChunk(int cx, int cy) {
		Chunk c = chunks.get(new Point(cx, cy));
		
		if(c == null)
			chunks.put(new Point(cx, cy), c = new Chunk(this, cx, cy, noiseGen));
		
		return c;
	}
	
	public Chunk getChunkFor(Vector pos) {
		return getChunk(pos.floorX() >> 4, pos.floorY() >> 4);
	}
	
	public GameObject requestPlayer() {
		
		Random rand = new Random();
		
		int x;
		int y;
		do {
			x = rand.nextInt(512) - 256;
			y = rand.nextInt(512) - 256;
		}while(!this.getLand(x, y).isSand());
		
		return new GameObject(this, "ent_player", x, y);
	}
	
	public synchronized ByteBuffer writeTo(ByteBuffer buf) {
		buf.putInt(noiseGen.getSeed()).putInt(chunks.size());
		
		LinkedList<GameObject> kgo = new LinkedList<>();
		
		for(Chunk c : chunks.values())
			synchronized(c) {
				c.writeTo(buf);
				c.addObjectsTo(kgo);
			}
		
		buf.putInt(kgo.size());
		
		for(GameObject go : kgo)
			go.writeTo(buf);
		
		return buf;
	}
	
	@Override public int writeSize() {
		int x = 1 << 6; //64
		//TODO Actually estimate and don't be lazy.
		return x << 10; //Returns in kilobytes
	}
	
	public boolean saveTo(File file) {
		try {
			ByteBuffer buf = ByteBuffer.allocate(writeSize());
			writeTo(buf);
			
			if(!buf.hasArray())
				return false;
			
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(buf.array(), 0, buf.position());
			fos.flush();
			fos.close();

			Logger.info("World saved.");
			Logger.debug("World size: " + buf.position());
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
}
