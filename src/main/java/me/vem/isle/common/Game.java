package me.vem.isle.common;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import me.vem.isle.common.controller.Controller;
import me.vem.isle.common.controller.PlayerController;
import me.vem.isle.common.eio.ExtResourceManager;
import me.vem.isle.common.objects.Property;
import me.vem.isle.common.world.World;
import me.vem.utils.logging.Logger;

public class Game {
	
	private static void init(File f) {
		worldFile = f;
		Property.register("tree", "player");
		Controller.register("plr_controller", PlayerController.class);
	}
	
	private static void postInit() {
		setInitialized();
		Logger.info("Game Startup Completed");
	}
	
	private static File worldFile;
	
	public static World newWorld(int seed) {
		init(getDefaultWorldFile());
		
		if(seed == 0)
			seed = new Random().nextInt();
		
		World world = new World(seed);
		
		postInit();
		
		return world;
	}
	
	public static World loadWorld(File f) {
		init(f);
		
		World world = World.loadFrom(f);
		if(world == null) {
			Logger.errorf("Failed loading world file '%s'!", f.getPath());
			return null;
		}
		
		Logger.infof("World file '%s' loaded!", f.getPath());
		
		postInit();
		
		return world;
	}
	
	public static File getWorldFile() {
		return worldFile;
	}
	
	private static File getDefaultWorldFile() {
		File f = ExtResourceManager.getDefaultWorldFile();
				
		File bckDir = ExtResourceManager.getBackupsDirectory();
		String newFileName = "world" + bckDir.listFiles().length + ".dat.bck";
		if(f.exists()) {
			f.renameTo(new File(bckDir, newFileName));
			f.delete();				
		}
		
		return f;
	}
	
	private static Map<Integer, RIdentifiable> byRUID = Collections.synchronizedMap(new HashMap<>());
	
	private static final AtomicInteger nextRUID = new AtomicInteger(1);
	public static void requestRUID(RIdentifiable rid) {
		int ruid = nextRUID.getAndIncrement();
		
		if(rid.setRUID(ruid))
			byRUID.put(ruid, rid);
	}
	
	public static RIdentifiable getByRUID(int ruid) {
		return byRUID.get(ruid);
	}
	
	private static boolean initialized;
	public static boolean isInitialized() { return initialized; }
	public static void setInitialized(){initialized = true;}
}