package com.tolgahankurtdere.spacefighter;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

import org.omg.CORBA.TRANSACTION_MODE;

import java.util.ArrayList;
import java.util.Random;

public class SpaceFighter extends ApplicationAdapter {
	SpriteBatch batch;
	Texture background,spaceShip1,enemy,laserBullet;
	BitmapFont font1,font2;
	Preferences preferences; //to save highScore

	float spaceShip1X,spaceShip1Y,spaceShip1Width,spaceShip1Height;
	float laserBulletWidth,laserBulletHeight;
	float enemyWidth,enemyHeight;

	final int spaceShipVelocity = 20;
	final int enemyVelocity = 4;
	int gameState = 0;
	int score = 0,health = 10,highScore;

	ArrayList<LaserBullet> laserBulletArrayList;
	ArrayList<Enemy> enemyArrayList;

	Random random;
	//ShapeRenderer shapeRenderer;

	@Override
	public void create () {
		preferences = Gdx.app.getPreferences("mypref");
		batch = new SpriteBatch();
		background = new Texture("background.jpg");
		spaceShip1 = new Texture("spaceShip1.png");
		enemy = new Texture("enemy.png");
		laserBullet = new Texture("laserBullet.png");

		font1 = new BitmapFont();
		font1.setColor(Color.WHITE);
		font1.getData().setScale(3);
		font2 = new BitmapFont();
		font2.setColor(Color.WHITE);
		font2.getData().setScale(6);

		laserBulletArrayList = new ArrayList<>();
		enemyArrayList = new ArrayList<>();

		spaceShip1X = Gdx.graphics.getWidth()/2 - spaceShip1.getWidth()/2;
		spaceShip1Y = 0;
		spaceShip1Width = Gdx.graphics.getWidth() / 12.5f;
		spaceShip1Height = Gdx.graphics.getHeight() / 8.5f;
		enemyWidth = Gdx.graphics.getWidth() / 12.5f;
		enemyHeight = Gdx.graphics.getHeight() / 10;
		laserBulletWidth = Gdx.graphics.getWidth()/85;
		laserBulletHeight = Gdx.graphics.getHeight()/35;

		random = new Random();
		//shapeRenderer = new ShapeRenderer();
		highScore = preferences.getInteger("highScore");

		new Thread(new Runnable() { //thread to fire every 100ms
			@Override
			public void run() {
				while (true){
					if(gameState == 1){
						laserBulletArrayList.add(new LaserBullet(spaceShip1X+spaceShip1Width/2-laserBulletWidth/2,spaceShip1Height));
					}
					for(int i=0;i<laserBulletArrayList.size();i++){ //if bullet go over the top border, delete it
						if(laserBulletArrayList.get(i).getY() >= Gdx.graphics.getHeight()){
							laserBulletArrayList.remove(i);
						}
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();

		new Thread(new Runnable() { //thread to create new enemies every 200ms
			@Override
			public void run() {
				while (true){
					if(gameState == 1){
						if(score > 75){ //if score>75, create double enemies
							enemyArrayList.add(new Enemy(random.nextInt((int) (Gdx.graphics.getWidth() - enemyWidth)),random.nextInt(Gdx.graphics.getHeight()*4) + Gdx.graphics.getHeight()));
						}
						enemyArrayList.add(new Enemy(random.nextInt((int) (Gdx.graphics.getWidth() - enemyWidth)),random.nextInt(Gdx.graphics.getHeight()) + Gdx.graphics.getHeight()));
					}
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();

	}

	@Override
	public void render () {
		batch.begin();
		batch.draw(background,0,0,Gdx.graphics.getWidth(),Gdx.graphics.getHeight()); //firstly draw background

		if(gameState == 0){ //Game is just opened
			font2.draw(batch,"Tap to start",Gdx.graphics.getWidth()/10,Gdx.graphics.getHeight()/2);
			if(Gdx.input.justTouched()){
				gameState = 1;
			}
		} //Game is just opened

		else if(gameState == 1){ //Game is started
			if(score > 150){ //if score>150 upgrade
				spaceShip1 = new Texture("spaceShip2.png");
				for(int i=0;i<laserBulletArrayList.size();i++){ //fire and draw lase bullets for big spaceship
					laserBulletArrayList.get(i).setY(laserBulletArrayList.get(i).getY()+laserBulletArrayList.get(i).getVelocity());
					batch.draw(laserBullet,laserBulletArrayList.get(i).getX()-spaceShip1Width/4,laserBulletArrayList.get(i).getY(),laserBulletWidth,laserBulletHeight);
					batch.draw(laserBullet,laserBulletArrayList.get(i).getX()+spaceShip1Width/4,laserBulletArrayList.get(i).getY(),laserBulletWidth,laserBulletHeight);
				}
			}
			else{
				for(int i=0;i<laserBulletArrayList.size();i++){ //fire and draw lase bullets for small spaceship
					laserBulletArrayList.get(i).setY(laserBulletArrayList.get(i).getY()+laserBulletArrayList.get(i).getVelocity());
					batch.draw(laserBullet,laserBulletArrayList.get(i).getX(),laserBulletArrayList.get(i).getY(),laserBulletWidth,laserBulletHeight);
				}
			} //if score>150 upgrade

			if(Gdx.input.isTouched()){ //touched right or left side
				if(Gdx.input.getX() < Gdx.graphics.getWidth() / 2){
					if(spaceShip1X > 0) spaceShip1X -= spaceShipVelocity;
				}
				else{
					if(spaceShip1X < Gdx.graphics.getWidth()-spaceShip1Width) spaceShip1X += spaceShipVelocity;
				}
			}

			for(int i=0;i<enemyArrayList.size();i++){ //move and draw enemies
				enemyArrayList.get(i).setY(enemyArrayList.get(i).getY() - enemyVelocity);
				batch.draw(enemy,enemyArrayList.get(i).getX(),enemyArrayList.get(i).getY(),enemyWidth,enemyHeight);
				if(enemyArrayList.get(i).getY() < 0){
					enemyArrayList.remove(i);
					health--;
				}
			}

			if(health <= 0){
				gameState = 2;
			}

			try{ //sometime whenHit() try to remove index which equals size
				whenHit();
			} catch (IndexOutOfBoundsException e){
				e.printStackTrace();
			}

		} //Game is started

		else if(gameState == 2){ // Game is over
			if (score > highScore) { //to arrange highScore
				highScore = score;
				preferences.putInteger("highScore", highScore);
				preferences.flush();
			}
			font2.draw(batch,"GAME OVER! Tap to play again",Gdx.graphics.getWidth()/10,Gdx.graphics.getHeight()/2);

			if(Gdx.input.justTouched()){ //restart the game
				gameState = 1;
				score = 0;
				health = 10;

				spaceShip1 = new Texture("spaceShip1.png");
				spaceShip1X = Gdx.graphics.getWidth()/2 - spaceShip1.getWidth()/2;
				laserBulletArrayList = new ArrayList<>();
				enemyArrayList = new ArrayList<>();
			}
		} // Game is over

		batch.draw(spaceShip1,spaceShip1X,spaceShip1Y,spaceShip1Width,spaceShip1Height);
		font1.draw(batch,"Score: " + String.valueOf(score),15,Gdx.graphics.getHeight() - 30);
		font1.draw(batch,"High Score: " + String.valueOf(highScore),Gdx.graphics.getWidth()/2-100,Gdx.graphics.getHeight() - 30);
		font1.draw(batch,"Health: " + String.valueOf(health),Gdx.graphics.getWidth() - 250,Gdx.graphics.getHeight() - 30);
		batch.end();

		/*.begin(ShapeRenderer.ShapeType.Filled);
		shapeRenderer.setColor(Color.BLACK);
		for(int i=0;i<enemyArrayList.size();i++){
			shapeRenderer.circle(enemyArrayList.get(i).getX()+enemyWidth/2,enemyArrayList.get(i).getY()+enemyHeight/2,enemyWidth/2);
		}
		for(int i=0;i<laserBulletArrayList.size();i++){
			shapeRenderer.circle(laserBulletArrayList.get(i).getX()+laserBulletWidth/2,laserBulletArrayList.get(i).getY()+laserBulletHeight,laserBulletWidth/2);
		}
		shapeRenderer.end();*/

	}
	
	@Override
	public void dispose () {

	}

	public void whenHit(){ // when player hit enemy, remove laser bullet and enemy, score++
		for(int i=0;i<laserBulletArrayList.size();i++){
			Circle circle = new Circle(laserBulletArrayList.get(i).getX()+laserBulletWidth/2,laserBulletArrayList.get(i).getY()+laserBulletHeight/2,laserBulletWidth/2);
			for(int j=0;j<enemyArrayList.size();j++){
				Circle circle1 = new Circle(enemyArrayList.get(j).getX()+enemyWidth/2,enemyArrayList.get(j).getY()+enemyHeight/2,enemyWidth/2);
				if(Intersector.overlaps(circle,circle1)){
					laserBulletArrayList.remove(i);
					enemyArrayList.remove(j);
					score++;
				}
			}
		}
	}
}

class LaserBullet{ //class of laser bullets
	private float x,y;
	private float width = Gdx.graphics.getWidth()/85,height = Gdx.graphics.getHeight()/35;
	private float velocity = 15;

	public LaserBullet(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public float getWidth() {
		return width;
	}

	public float getHeight() {
		return height;
	}

	public float getVelocity() {
		return velocity;
	}
}

class Enemy{ //class of enemies
	private float x,y;
	private float width = Gdx.graphics.getWidth() / 12.5f,enemyHeight = Gdx.graphics.getHeight() / 10;
	private float velocity = 4;

	public Enemy(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public float getWidth() {
		return width;
	}

	public void setWidth(float width) {
		this.width = width;
	}

	public float getEnemyHeight() {
		return enemyHeight;
	}

	public void setEnemyHeight(float enemyHeight) {
		this.enemyHeight = enemyHeight;
	}

	public float getVelocity() {
		return velocity;
	}

	public void setVelocity(float velocity) {
		this.velocity = velocity;
	}
}
