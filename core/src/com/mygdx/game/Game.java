package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Iterator;


public class Game extends ApplicationAdapter {
	private Texture dropImage;
	private Texture bucketImage;
	private Sound dropSound;
	private Music rainMusic;
	private Rectangle bucket;
	private OrthographicCamera camera;
	private SpriteBatch batch;
	private Input Input;
	private Array<Rectangle> raindrops; //Gdx class to be used instead of array, better garbage-collection
	private long lastDropTime; //time in ms, long numbers

	@Override
	public void create () {
		/* Screen is 800 by 480px*/
		// load the images for the droplet and the bucket, 64x64 pixels each
		dropImage = new Texture(Gdx.files.internal("drop.png"));
		bucketImage = new Texture(Gdx.files.internal("bucket.png"));

		// load the drop sound effect and the rain background "music"
		dropSound = Gdx.audio.newSound(Gdx.files.internal("waterdrop.wav"));
		rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));

		// start the playback of the background music immediately
		rainMusic.setLooping(true);
		rainMusic.play();

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

	@Override
	public void render () { //runs in between frames
		ScreenUtils.clear(0, 0, 0.2f, 1); // background color
		camera.update();

		//Draw bucket
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		batch.draw(bucketImage, bucket.x, bucket.y);
		for(Rectangle raindrop: raindrops) {
			batch.draw(dropImage, raindrop.x, raindrop.y);
		}
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
			if (raindrop.overlaps(bucket)) {
				dropSound.play();
				iter.remove();
			}
		}
	}
	
	@Override
	public void dispose () {
		// dispose of all the native resources
		dropImage.dispose();
		bucketImage.dispose();
		dropSound.dispose();
		rainMusic.dispose();
		batch.dispose();
	}
}
