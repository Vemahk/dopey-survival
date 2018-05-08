package me.isle.game;

import java.util.Random;

import gustavson.simplex.SimplexNoise;
import me.isle.game.entity.PlayerEntity;
import me.isle.game.land.Grass;
import me.isle.game.land.Land;
import me.isle.game.land.Water;
import me.isle.game.objects.Tree;
import me.isle.graphics.ArrowKeyListener;

public class Game {

	public static boolean DEBUG_ACTIVE = false;
	public static Game game;
	public static Random rand = new Random();
	
	private final int WIDTH;
	private final int HEIGHT;
	
	private Land[][] land;
	
	private ArrowKeyListener akl;
	
	private PlayerEntity player;
	
	public Game() {
		WIDTH = 512;
		HEIGHT = 512;
		
		SimplexNoise sn = new SimplexNoise(300, .5, rand.nextInt());
    	
    	double[][] res = new double[WIDTH][HEIGHT];
    	
    	for(int x=0;x<WIDTH;x++) {
    		for(int y=0;y<HEIGHT;y++) {
                res[x][y]=0.5*(1+sn.getNoise(x,y));
    		}
    	}
		
		land = new Land[WIDTH][HEIGHT];
		for(int x=0;x<WIDTH;x++) {
			for(int y=0;y<HEIGHT;y++) {
				if(res[x][y] >= .55 && Math.random()<.2)
					new Tree(x+.5, y+.5).initialize();
				if(res[x][y] >= .52)
					land[x][y] = new Grass(x, y);
				else if(res[x][y] >= .5)
					land[x][y] = new Land(x, y);
				else land[x][y] = new Water(x, y);
			}
		}
		
		int x = 256;
		int y = 256;
		for(;land[x][y] instanceof Water;
			 x = rand.nextInt(256) + 128, 
			 y = rand.nextInt(256) + 128);
		
		player = (PlayerEntity) new PlayerEntity(x, y).initialize();
	}
	
	public PlayerEntity getPlayer() {
		return player;
	}
	
	public int getWidth() {
		return WIDTH;
	}
	
	public int getHeight() {
		return HEIGHT;
	}
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public Land getLand(int x, int y) {
		return land[x][y];
	}
	
	public ArrowKeyListener getKeyListener() {
		return akl;
	}
	
	public ArrowKeyListener setKeyListener(ArrowKeyListener akl) {
		this.akl = akl;
		return akl;
	}
}
