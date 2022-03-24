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
	private Rectangle bucket;
	private OrthographicCamera camera;
	private SpriteBatch batch;
	private Input Input;
	private Array<Rectangle> raindrops; //Gdx class to be used instead of array, better garbage-collection
	private long lastDropTime; //time in ms, long numbers
	private int score;
	private BitmapFont font;

	@Override
	public void create () { //setup
		/* Screen is 800 by 480px*/
		// load the images for the droplet and the bucket, 64x64 pixels each
		dropImage = new Texture(Gdx.files.internal("drop.png"));
		bucketImage = new Texture(Gdx.files.internal("bucket.png"));

		//create camera
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 480);

		batch = new SpriteBatch();

		//create bucket rectangle
		bucket = new Rectangle();
		bucket.x = 800 / 2 - 64 / 2;//centers rectangle horizontally
		bucket.y = 20;
		bucket.width = 64;
		bucket.height = 64;

		raindrops = new Array<Rectangle>();
		spawnRaindrop();

		//font
		font = new BitmapFont(Gdx.files.internal("SilomFont.fnt"));
		score = 0;
	}

	//make rain drops
	private void spawnRaindrop() {
		Rectangle raindrop = new Rectangle();
		raindrop.x = MathUtils.random(0, 800-64); //return rand value from 0 to 746
		raindrop.y = 480; //set y at the top of the screen
		raindrop.width = 64;
		raindrop.height = 64;
		raindrops.add(raindrop); //add to array raindrops
		lastDropTime = TimeUtils.nanoTime(); //
	}

	private void spawnBall(){
		//Ball ball = new Ball(1,2);
		Random rand = new Random();
		int operatorNum = rand.nextInt(2);
		int ballValue = rand.nextInt(10);
		switch (operatorNum){
			case 0:
				AdditionBall ball1 = new AdditionBall(ballValue);
				break;
			case 1:
				SubtractionBall ball2 = new SubtractionBall(ballValue);
				break;
			case 2:
				MultiplicationBall ball3 = new MultiplicationBall(ballValue);
				break;
		}
		//fill in display
	}

	/* ideia:
	Objeto ball criado com certe frequencia; contem todos valores necessarios.
	Mantem o objeto Circle, usado para display.

	Precisa chegar mais perto possivel a um numero, # de bolas fixo (10)

 	*/
	

	public class Ball{
		Circle displayObject;
		int numberValue;

		public Ball(int num){ //constructor
			//better way to do this - call w/ a string
			numberValue = num;
			displayObject = new Circle(); //actual object on screen
		}

		private void display(){
			System.out.println(2);
			//show on screen, calls calcDisplayValue
		}

		void update(){
			System.out.println(this.numberValue);
			//move down / update on circle object
		}
	}

	public class AdditionBall extends Ball{

		public AdditionBall(int num) {
			super(num);
		}

		void update(){
			super.update();
		}

	}

	public class SubtractionBall extends Ball{

		public SubtractionBall(int num) {
			super(num);
		}

		void update(){
			super.update();
		}

	}

	public class MultiplicationBall extends Ball{

		public MultiplicationBall(int num) {
			super(num);
		}

		void update(){
			super.update();
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
		for(Rectangle raindrop: raindrops) {
			batch.draw(dropImage, raindrop.x, raindrop.y);
		}
		font.draw(batch, Integer.toString(score), 760, 460);
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
		if(bucket.x > 800 - 64) bucket.x = 800 - 64;

		//if too much time has passed, it spawns a new raindrop
		if(TimeUtils.nanoTime() - lastDropTime > 1000000000) spawnRaindrop();

		for (Iterator<Rectangle> iter = raindrops.iterator(); iter.hasNext(); ) { //iterate through raindrops
			Rectangle raindrop = iter.next();
			raindrop.y -= 200 * Gdx.graphics.getDeltaTime();
			if (raindrop.y + 64 < 0) iter.remove();
			if (raindrop.overlaps(bucket)) { //collision
				iter.remove();
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
