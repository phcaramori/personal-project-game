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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;
import java.util.Iterator;
import java.util.Random;


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

	private int score;
	private int mainValue;
	private BitmapFont font;

	/*
	-	não sei se devo usar Circle ou Rectangle com foto de um circulo
	 */

	@Override
	public void create () { //setup
		/* Screen is 800 by 480px*/
		// load the images for the droplet and the bucket, 64x64 pixels each
		dropImage = new Texture(Gdx.files.internal("drop.png"));
		bucketImage = new Texture(Gdx.files.internal("bucket.png"));

		//create camera
		float screenWidth = Gdx.graphics.getWidth();
		float screenHeight = Gdx.graphics.getHeight();
		camera = new OrthographicCamera(width, height * (screenHeight / screenWidth));
		camera.setToOrtho(false, width, height * (screenHeight / screenWidth));

		batch = new SpriteBatch();

		//create bucket circle
		bucket = new Circle();
		bucket.x = 800 / 2 - 64 / 2;//centers rectangle horizontally
		bucket.y = 20;
		bucket.radius = 64;


		//font
		font = new BitmapFont(Gdx.files.internal("SilomFont.fnt"));
		score = 0;
		mainValue = 0;


		balls = new Array<Ball>();
	}

	private void spawnBall(){
		//Ball ball = new Ball(1,2);
		Random rand = new Random();
		int ballValue;
		int operatorNum = rand.nextInt(3);
		switch (operatorNum){
			case 0:
				ballValue = rand.nextInt(20);
				AdditionBall ball1 = new AdditionBall(ballValue);
				balls.add(ball1);
				break;
			case 1:
				ballValue = rand.nextInt(20);
				SubtractionBall ball2 = new SubtractionBall(ballValue);
				balls.add(ball2);
				break;
			case 2:
				ballValue = rand.nextInt(5);
				MultiplicationBall ball3 = new MultiplicationBall(ballValue);
				balls.add(ball3);
				break;
		}
		//fill in display
	}


	/*
	-	Ball has 3 subclasses: AdditionBall, Ball, and MultiplicationBall
	-	The display functions are handled by the superClass
	-	Collision is inherited from the superClass
	-	Each subclass' constructor is derived from the super, using the super() method
	-	The operator-specific functions are in each individual subclass
	-	Each subclass inherits all of the super class's methods
	 */
	public class Ball{
		Circle displayObject; //object seen on screen - libGDX circle API
		int numberValue;

		public Ball(int num){ //constructor
			numberValue = num; //Number value of ball - subclasses handle operations
			displayObject = new Circle(); //actual object on screen
			displayObject.x = MathUtils.random(0, 800-64); //return rand value from 0 to 746
			displayObject.y = 480; //set y at the top of the screen
			displayObject.radius = 64; //radius/size of circle
			lastDropTime = TimeUtils.nanoTime();
		}

		void collideS(String operator, int val){ //Called on collision
			if(operator == "add"){
				mainValue += val;
			}else if(operator == "subtract"){
				mainValue -= val;
			}else if(operator == "multiply"){
				mainValue *= val;
			}
			System.out.println(mainValue);
		}
	}

	public class AdditionBall extends Ball{
		int value;

		public AdditionBall(int num) {
			super(num);
			value = num;
			System.out.println("addition ball created");
		}

		void collide(){
			super.collideS("add", value);
			System.out.println("ADDING " + value + "TO " + mainValue);
		}

	}

	public class SubtractionBall extends Ball{
		int value;

		public SubtractionBall(int num) {
			super(num);
		}

		void collide(){
			super.collideS("subtract", value);
		}

	}

	public class MultiplicationBall extends Ball{
		int value;

		public MultiplicationBall(int num) {
			super(num);
		}

		void collide(){
			super.collideS("multiply", value);
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
		for(Ball ball: balls) {
			batch.draw(dropImage, ball.displayObject.x, ball.displayObject.y);
		}
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
			if (ball.displayObject.y + 64 < 0) iter.remove();
			if (ball.displayObject.overlaps(bucket)) { //collision
				iter.remove();
				System.out.println(balls);
				score ++;
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
