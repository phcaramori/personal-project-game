package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;

import java.awt.*;
import java.util.Iterator;
import java.util.Random;

/**
 * TO-DO:
 * add text in circles
 * add text at top of screen saying goal number (100)
 * add text saying number of balls remaining
 * add logic to ball maker to only drop if ball remaining !<=0
 * add logic to see how close ball was from 100 - make score the distance
 */
public class Game extends ApplicationAdapter {
	private Texture dropImage;
	private Texture bucketImage;
	private Circle bucket;
	private OrthographicCamera camera;
	private SpriteBatch batch;
	private Input Input;
 	private Array<Ball> balls;
	private long lastDropTime; //time in ms, long numbers
	int width = 480;
	int height = 800;
	private ShapeRenderer shapeRenderer;

	private int score;
	private int mainValue;
	private BitmapFont font;

	@Override
	public void create () { //setup
		/* Screen is 800 by 480px*/
		// load the images for the droplet and the bucket, 64x64 pixels each
		bucketImage = new Texture(Gdx.files.internal("bucket.png"));

		//create camera
		float screenWidth = Gdx.graphics.getWidth();
		float screenHeight = Gdx.graphics.getHeight();
		camera = new OrthographicCamera(width, height * (screenHeight / screenWidth));
		camera.setToOrtho(false, width, height * (screenHeight / screenWidth));

		batch = new SpriteBatch();
		shapeRenderer = new ShapeRenderer();

		//create bucket circle
		bucket = new Circle();
		bucket.radius = 64;
		bucket.x = Gdx.graphics.getWidth() / 2 - bucket.radius / 2;//centers rectangle horizontally
		bucket.y = 20;


		//font
		font = new BitmapFont(Gdx.files.internal("SilomFont.fnt"));
		score = 0;
		mainValue = 10;


		balls = new Array<Ball>();
	}

	private void spawnBall(){
		//Ball ball = new Ball(1,2);
		Random rand = new Random();
		int ballValue;
		int operatorNum = rand.nextInt(3);
		if(operatorNum == 2){
			ballValue = rand.nextInt(5);//max value of 5 for multiplication balls
		} else {
			ballValue = rand.nextInt(30);//max value of 30 for addition and subtraction balls
		}
		Ball ball1 = new Ball(ballValue,operatorNum);
		balls.add(ball1);
	}

	public class Ball{
		Circle displayObject; //object seen on screen - libGDX circle API
		int numberValue;
		int operator; // 0: Addition, 1: Subtraction, 2: Multiplication

		public Ball(int num, int type){ //constructor
			numberValue = num; //Number value of ball - subclasses handle operations
			displayObject = new Circle(); //actual object on screen
			displayObject.radius = 32; //radius/size of circle
			displayObject.x = MathUtils.random(0, Gdx.graphics.getWidth()-displayObject.radius);
			displayObject.y = Gdx.graphics.getHeight(); //set y at the top of the screen
			lastDropTime = TimeUtils.nanoTime();
			operator = type;
			System.out.println("value is now" + mainValue);
		}

		void renderBall () {
			shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
			shapeRenderer.setColor(0,0,1,1);
			shapeRenderer.circle(displayObject.x, displayObject.y, displayObject.radius);
			shapeRenderer.end();
		}
	}

	@Override
	public void render () { //runs in between frames
		ScreenUtils.clear(0, 0, 0.2f, 1); // background color
		camera.update();

		//Draw bucket
		batch.setProjectionMatrix(camera.combined);


		//batch
		batch.begin();
		batch.draw(bucketImage, bucket.x, bucket.y);
		font.draw(batch, Integer.toString(score), Gdx.graphics.getWidth()-30, Gdx.graphics.getHeight()*2-30);
		batch.end(); //sends commands all at once. All "draw" must be in-between .begin and .end


		//inputs
		if(Gdx.input.isTouched()) { // touch / mouse click
			/* Next we want to transform the touch/mouse coordinates to our camera’s coordinate system.
			This is necessary because the coordinate system in which touch/mouse coordinates are reported
			might be different than the coordinate system we use to represent objects in our world.*/

			Vector3 touchPos = new Vector3(); //make touchPos a private final field of the Drop class
			//system of 3 vectors (x,y,z). z is always 0 since this is not 3D
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			// Gdx.input returns position of touch
			camera.unproject(touchPos);
			/* camera.unproject takes in a vector3,and returns the
			location of the click using the Game's coordinate system */
			bucket.x = touchPos.x - 64 / 2;
			//center bucket around touch, ONLY ON X AXIS
		}
		//keyboard input
		if(Gdx.input.isKeyPressed(Keys.LEFT)) bucket.x -= 200 * Gdx.graphics.getDeltaTime();
		if(Gdx.input.isKeyPressed(Keys.RIGHT)) bucket.x += 200 * Gdx.graphics.getDeltaTime();
		//getDeltaTime returns time since last frame in seconds
		if(bucket.x < 0) bucket.x = 0;
		if(bucket.x > Gdx.graphics.getWidth() - 64) bucket.x = Gdx.graphics.getWidth() - 64;

		//if too much time has passed, it spawns a new raindrop
		if(TimeUtils.nanoTime() - lastDropTime > 2000000000) spawnBall();

		for (Iterator<Ball> iter = balls.iterator(); iter.hasNext(); ) { //iterate through raindrops
			Ball ball = iter.next();
			ball.displayObject.y -= 200 * Gdx.graphics.getDeltaTime();
			ball.renderBall();
			if (ball.displayObject.y + 64 < 0) iter.remove();
			if (ball.displayObject.overlaps(bucket)) { //collision
				iter.remove();
				if(ball.operator == 0){
					mainValue += ball.numberValue;
					System.out.println("Ball added " +ball.numberValue+" to value");
				}else if(ball.operator == 1){
					mainValue -= ball.numberValue;
					System.out.println("Ball subtracted " +ball.numberValue+" to value");
				}else if(ball.operator == 2){
					mainValue *= ball.numberValue;
					System.out.println("Ball multiplied " +ball.numberValue+" to value");
				}

				if(mainValue < 0){ //dont allow mainValue to drop below 0
					mainValue = 0;
				}
			}
		}
	}


	@Override
	public void dispose () {
		// dispose of all the native resources
		dropImage.dispose();
		bucketImage.dispose();
		batch.dispose();
	}
}
