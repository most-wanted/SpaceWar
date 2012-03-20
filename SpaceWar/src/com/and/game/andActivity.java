package com.and.game;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.camera.hud.controls.AnalogOnScreenControl;
import org.anddev.andengine.engine.camera.hud.controls.AnalogOnScreenControl.IAnalogOnScreenControlListener;
import org.anddev.andengine.engine.camera.hud.controls.BaseOnScreenControl;
import org.anddev.andengine.engine.camera.hud.controls.BaseOnScreenControl.IOnScreenControlListener;
import org.anddev.andengine.engine.camera.hud.controls.DigitalOnScreenControl;
import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.engine.handler.physics.PhysicsHandler;
import org.anddev.andengine.engine.handler.timer.ITimerCallback;
import org.anddev.andengine.engine.handler.timer.TimerHandler;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.modifier.MoveModifier;
import org.anddev.andengine.entity.modifier.MoveXModifier;
import org.anddev.andengine.entity.modifier.ScaleModifier;
import org.anddev.andengine.entity.modifier.SequenceEntityModifier;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.ui.activity.BaseGameActivity;

import android.view.Display;
import javax.microedition.khronos.opengles.GL10;

public class andActivity extends BaseGameActivity {
	//private
	private Camera mCamera;
	private Scene mainScene;
    private BitmapTextureAtlas bitmap;
    private TiledTextureRegion playTextureRegion;
    private TextureRegion enemyTextureRegion;
    // controller
    private BitmapTextureAtlas onScreenControlTexture;
    private BitmapTextureAtlas fireControlTexture;
    private TextureRegion onScreenControlBase;
    private TextureRegion onScreenControlKnob;
    private TextureRegion xBtnTexture;
    private TextureRegion trBtnTexture;
    private TextureRegion oBtnTexture;
    private TextureRegion sqBtnTexture;
    private TextureRegion bulletTexture;
    private LinkedList enemies;
    private LinkedList enemiesToBeAdded;
    private AnimatedSprite player;
    private Sprite xBtn;
    private Sprite trBtn;
    private Sprite oBtn;
    private Sprite sqBtn;
    static int currentSpriteX = 0;
    static int currentSpriteY = 0;
    // the pool
    private SpritesPool SpritePool;
	@Override
	public Engine onLoadEngine() {
		final Display display = getWindowManager().getDefaultDisplay();
		mCamera = new Camera(0, 0, display.getWidth(), display.getHeight());
		
		return new Engine(new EngineOptions(true, ScreenOrientation.LANDSCAPE,
				         new RatioResolutionPolicy(mCamera.getWidth(), mCamera.getHeight()),
				         mCamera).setNeedsMusic(true).setNeedsSound(true));
	}
	@Override
	public void onLoadResources() {
	
		bitmap = new BitmapTextureAtlas(512, 512, // Resolution
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		// for controller
     	onScreenControlTexture = new BitmapTextureAtlas(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
     	fireControlTexture = new BitmapTextureAtlas(512, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		playTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
				bitmap, this, "helicopter.png", 0, 0,2,2);
		playTextureRegion.setFlippedHorizontal(true);
		enemyTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
				bitmap, this, "Target.png", 128, 0);
		bulletTexture = BitmapTextureAtlasTextureRegionFactory.createFromAsset
		(bitmap, this, "Projectile.png",256,0);
		/////the pool
		SpritePool = new SpritesPool(enemyTextureRegion);
		
		///////////////// for controller /////////
		onScreenControlBase = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
				onScreenControlTexture, this, "onscreen_control_base.png",0,0);
		onScreenControlKnob = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
				onScreenControlTexture, this, "onscreen_control_knob.png",128,0);
	xBtnTexture = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
				fireControlTexture, this, "xPS.png",0,0);
	oBtnTexture = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
			fireControlTexture, this, "oPS.png",128,0);
	trBtnTexture = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
			fireControlTexture, this, "trPS.png",256,0);
	sqBtnTexture = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
			fireControlTexture, this, "squarePS.png",384,0);
		///////////////////////////////////////////////////////////
	    mEngine.getTextureManager().loadTextures(bitmap, onScreenControlTexture,fireControlTexture);	
	}

	@Override
	public Scene onLoadScene() {
		mEngine.registerUpdateHandler(new FPSLogger());
		mainScene = new Scene();
		mainScene.setBackground(new ColorBackground(0.09f, 0.6f, 0.8f));
		AddThePlayer();
		final PhysicsHandler physicsHandler = new PhysicsHandler(player);
		player.registerUpdateHandler(physicsHandler);
		AddTheFireButtons();
		AddTheAnalogControler(physicsHandler);
		createEnemiesTimeHandler(); // create random enemies 
		mainScene.registerUpdateHandler(detectSpriteOutOfScreen); // detect when outside
		return mainScene;
	}

	@Override
	public void onLoadComplete() {
		
		
	}
    public void AddEnemey()
    {
    	Random rand = new Random();
    	int x = (int)mCamera.getWidth() + enemyTextureRegion.getWidth();
    	int minY = enemyTextureRegion.getHeight();
    	int maxY = (int)mCamera.getHeight() - minY;
    	int rangY = maxY - minY;
    	int y = rand.nextInt(rangY) + minY; //  random y in range
    	currentSpriteX = x;
    	currentSpriteY = y;
    	Sprite target = SpritePool.obtainPoolItem();  // get Sprite from the pool
    	mainScene.attachChild(target);
    	int minSpeed = 2;
    	int maxSpeed = 4;
    	int speedNow = rand.nextInt(maxSpeed - minSpeed)+minSpeed;
    	MoveXModifier mov = new MoveXModifier(speedNow, target.getX(), -target.getHeight());
    	target.registerEntityModifier(mov.deepCopy());
    	enemiesToBeAdded.add(target);
    	
    }
    public void AddBullet()
    {
    	Sprite bullet = new Sprite(player.getX()+ player.getWidth(), player.getY()+ player.getHeight() / 2, bulletTexture.deepCopy());
    	mainScene.attachChild(bullet);
    	int speed = 3;
    	MoveXModifier mov = new MoveXModifier(speed, bullet.getX(), mCamera.getWidth());
    	bullet.registerEntityModifier(mov.deepCopy());
    }
    // create new enemies periodically
    public void createEnemiesTimeHandler()
    {
    	enemies = new LinkedList();
		enemiesToBeAdded = new LinkedList();
    	TimerHandler spriteTimesHandler;
    	float delay = 1f;
    	spriteTimesHandler = new TimerHandler(delay, true,new ITimerCallback() {
			
			@Override
			public void onTimePassed(TimerHandler pTimerHandler) {
				AddEnemey();
				
			}
		});
    	getEngine().registerUpdateHandler(spriteTimesHandler);
    }
   
    // detect when Sprite gets out of screen
    IUpdateHandler detectSpriteOutOfScreen = new IUpdateHandler() {
		
		@Override
		public void reset() {
		}
		@Override
		public void onUpdate(float pSecondsElapsed) {
			Iterator<Sprite> it = enemies.iterator();
			Sprite enemy;
			while(it.hasNext())
			{
				enemy = it.next(); // enemy to be deleted of it is out of screen
				if(enemy.getX() <= -enemy.getWidth())
				{
					SpritePool.recyclePoolItem(enemy);
					it.remove();
				}
			}
			enemies.addAll(enemiesToBeAdded);
			enemiesToBeAdded.clear();	
		}
	};

	public void AddTheAnalogControler(final PhysicsHandler physicsHandler)
	{
			IAnalogOnScreenControlListener IanalogController= new IAnalogOnScreenControlListener() {
			
			@Override
			public void onControlChange(BaseOnScreenControl pBaseOnScreenControl,
					float pValueX, float pValueY) {
			
				if(pValueX < 0 && player.getX() <= 5)
				{
					pValueX = 0;
				}
				if(pValueY < 0 && player.getY() <= 5)
				{
					pValueY = 0;
				}
				if(pValueY > 0 && player.getY() >= mCamera.getHeight()- player.getHeight()-5)
				{
					pValueY = 0;
				}
				if(pValueX > 0 && player.getX() >= mCamera.getWidth()- player.getWidth()-5)
				{
					pValueX = 0;
				}
				physicsHandler.setVelocity(pValueX * 200, pValueY * 200);
			}
			@Override
			public void onControlClick(AnalogOnScreenControl pAnalogOnScreenControl) {
	
			}
		};
		final AnalogOnScreenControl analogOnScreenControl = new AnalogOnScreenControl(0f, 
				mCamera.getHeight() - onScreenControlBase.getHeight(), mCamera, 
				onScreenControlBase, onScreenControlKnob, 0.1f, 200, IanalogController);
		analogOnScreenControl.getControlBase().setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
        analogOnScreenControl.getControlBase().setAlpha(0.5f);
        analogOnScreenControl.getControlBase().setScaleCenter(0, 64);
        analogOnScreenControl.getControlBase().setScale(1f);
        analogOnScreenControl.getControlKnob().setScale(1f);
        analogOnScreenControl.refreshControlKnobPosition();

        mainScene.setChildScene(analogOnScreenControl);
		
	}
	public void AddThePlayer()
	{
		
		int playerX = playTextureRegion.getWidth() / 2;
		int playerY = (int)(mCamera.getHeight() - playTextureRegion.getHeight()) / 2;
		player = new AnimatedSprite(playerX, playerY, playTextureRegion);
		player.animate(new long[] { 100, 100 }, 1, 2, true);
		final PhysicsHandler physicsHandler = new PhysicsHandler(player);
		player.registerUpdateHandler(physicsHandler);		
		mainScene.attachChild(player);
	}
	public void AddTheFireButtons()
	{		
		AddTheXButton();
		AddTheOButton();
		AddTheTrButton();
		AddTheSquareButton();
		mainScene.setTouchAreaBindingEnabled(true);
	}
	public void AddTheXButton()
	{
		xBtn = new Sprite(mCamera.getWidth()-2*xBtnTexture.getWidth(),
				mCamera.getHeight()-xBtnTexture.getHeight(), xBtnTexture){
		      @Override
              public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
            		  final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
                      runOnUpdateThread(new Runnable() {
                      @Override
                      public void run() {
                    	 
                          AddBullet();
                      }
              });
                      return false;
              }
			
		};
		mainScene.registerTouchArea(xBtn);
		mainScene.attachChild(xBtn);
	}
	public void AddTheOButton()
	{
		oBtn = new Sprite(mCamera.getWidth()-xBtnTexture.getWidth(),
				mCamera.getHeight()-1.5f*xBtnTexture.getHeight(), oBtnTexture){
			
		      @Override
              public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
            		  final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
                      runOnUpdateThread(new Runnable() {
                      @Override
                      public void run() {     	  
                           player.stopAnimation();
                      }
              });
                      return false;
              }
		};
		mainScene.registerTouchArea(oBtn);
	    mainScene.attachChild(oBtn);
	}
	public void AddTheTrButton()
	{
		trBtn = new Sprite(mCamera.getWidth()-2*xBtnTexture.getWidth(),
				mCamera.getHeight()-2f*xBtnTexture.getHeight(), trBtnTexture){
			
		      @Override
              public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
            		  final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
                      runOnUpdateThread(new Runnable() {
                      @Override
                      public void run() {     	  
                           player.stopAnimation();
                      }
              });
                      return false;
              }
		};
		mainScene.registerTouchArea(trBtn);
	    mainScene.attachChild(trBtn);	
	}
	public void AddTheSquareButton()
	{
		sqBtn = new Sprite(mCamera.getWidth()-3*xBtnTexture.getWidth(),
				mCamera.getHeight()-1.5f*xBtnTexture.getHeight(), sqBtnTexture){
			
		      @Override
              public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
            		  final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
                      runOnUpdateThread(new Runnable() {
                      @Override
                      public void run() {     	  
                           player.stopAnimation();
                      }
              });
                      return false;
              }
		};
		mainScene.registerTouchArea(sqBtn);
	    mainScene.attachChild(sqBtn);
		
	}
  
}