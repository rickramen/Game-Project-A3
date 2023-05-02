package a3;

import tage.*;
import tage.nodeControllers.*;
import tage.shapes.*;
import tage.input.*; 
import tage.input.action.*; 

import java.lang.Math;
import java.awt.*;
import java.awt.event.*;
import java.io.*;


import org.joml.*;

import java.util.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

import java.net.InetAddress;
import java.net.UnknownHostException;

import net.java.games.input.*; 
import net.java.games.input.Component.Identifier.*; 
import tage.networking.IGameConnection.ProtocolType;

import javax.swing.*;
import javax.swing.plaf.metal.OceanTheme;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.Invocable;

import tage.physics.PhysicsEngine;
import tage.physics.PhysicsObject;
import tage.physics.PhysicsEngineFactory;
import tage.physics.JBullet.*;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.collision.dispatch.CollisionObject;

import tage.audio.AudioManagerFactory;
import tage.audio.AudioResource;
import tage.audio.AudioResourceType;
import tage.audio.IAudioManager;
import tage.audio.Sound;
import tage.audio.SoundType;
import tage.audio.joal.JOALAudioManager;



public class MyGame extends VariableFrameRateGame
{
	private static Engine engine;
	private InputManager im;
	private GhostManager gm;
	private NodeController rc, bc;

	// ----- Scripts -----
	private File scriptFile1;
	private long fileLastModifiedTime = 0;
	ScriptEngine jsEngine;

	// ---- Camera ----
	private Camera cam;
	private Camera vpCam;
	private CameraOrbit3D orbitController;

	private double startTime, prevTime, elapsedTime, deltaTime;
	private int health;

	private boolean isAxesOn;
	private int lakeIslands;
	private Light light1;

	private double avatarPosX, avatarPosY, avatarPosZ, avatarScale;
	private double zombieScale;
	private double rotationSpeed, bounceSpeed;
	private double terrainLocX, terrainLocY, terrainLocZ, terrainScaleX, terrainScaleY, terrainScaleZ;



	// ---- GameObject Declarations ----
	private GameObject x, y, z;
	private ObjShape linxS, linyS, linzS;

	private GameObject terr;
	private ObjShape terrS;
	private TextureImage terrtx;
	private TextureImage hills;

	private GameObject avatar, zombie;
	private ObjShape ghostS, zombieS, robotS;
	private TextureImage ghostT, zombietx, robottx;

	private GameObject sphere;
	private ObjShape  sphereS;
	private TextureImage spheretx;


	// Server
	private String serverAddress;
	private int serverPort;
	private ProtocolType serverProtocol;
	private ProtocolClient protClient;
	private boolean isClientConnected = false;

	// Physics Engine
	private GameObject grenade1, plane;
	private ObjShape grenadeS, planeS;
	private TextureImage grenadetx;
	
	private PhysicsEngine physicsEngine;
	private PhysicsObject grenade1P, planeP;

	// Sound
	private IAudioManager audioMgr;
	private Sound ambientSound, zombieSound;

	private boolean running = true;
	private float vals[] = new float[16];

	public MyGame(String serverAddress, int serverPort, String protocol)
	{	super();
		gm = new GhostManager(this);
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
		if (protocol.toUpperCase().compareTo("TCP") == 0)
			this.serverProtocol = ProtocolType.TCP;
		else
			this.serverProtocol = ProtocolType.UDP;
	}

	public static void main(String[] args)
	{	MyGame game = new MyGame(args[0], Integer.parseInt(args[1]), args[2]);
		engine = new Engine(game);
		game.initializeSystem();
		game.game_loop();
	}

	@Override
	public void loadShapes()
	{	
		linxS = new Line(new Vector3f(0f,0f,0f), new Vector3f(3f,0f,0f)); 
  		linyS = new Line(new Vector3f(0f,0f,0f), new Vector3f(0f,3f,0f)); 
  		linzS = new Line(new Vector3f(0f,0f,0f), new Vector3f(0f,0f,-3f));

		terrS = new TerrainPlane(1500);

		zombieS = new ImportedModel("zombie.obj");
		robotS = new ImportedModel("robot2.obj");
		ghostS = new ImportedModel("zombie.obj");

		sphereS = new Sphere();

		grenadeS = new Sphere();
		planeS = new Plane();
	
	}

	@Override
	public void loadTextures()
	{	
		terrtx = new TextureImage("foliage.png");
		hills = new TextureImage("heightmap.png");

		zombietx = new TextureImage("zombie.png");
		robottx = new TextureImage("robotunwraped.png");
		ghostT = new TextureImage("zombie.png");
		spheretx = new TextureImage("sob.png");

		grenadetx = new TextureImage("grenade.png");
	

	}

	@Override
	public void loadSkyBoxes(){
		lakeIslands = (engine.getSceneGraph()).loadCubeMap("lakeIslands");
		(engine.getSceneGraph()).setActiveSkyBoxTexture(lakeIslands);
		(engine.getSceneGraph()).setSkyBoxEnabled(true);
	}

	@Override
	public void buildObjects()
	{	
		Matrix4f initialTranslation, initialScale;
		Random rand = new Random();

		// Initialize JavaScript engine
		ScriptEngineManager factory = new ScriptEngineManager();
		jsEngine = factory.getEngineByName("js");

		// Initialize parameters in InitParams.js
		scriptFile1 = new File ("assets/scripts/InitParams.js");
		this.runScript(scriptFile1);

		// ScriptEngine variables
		avatarPosX = ((double)(jsEngine.get("avatarPosX")));
		avatarPosY = ((double)(jsEngine.get("avatarPosY")));
		avatarPosZ = ((double)(jsEngine.get("avatarPosZ")));
		avatarScale = ((double)(jsEngine.get("avatarScale")));
		zombieScale = ((double)(jsEngine.get("zombieScale")));
		terrainLocX = ((double)(jsEngine.get("terrainLocX")));
		terrainLocY= ((double)(jsEngine.get("terrainLocY")));
		terrainLocZ= ((double)(jsEngine.get("terrainLocZ")));
		terrainScaleX = ((double)(jsEngine.get("terrainScaleX")));
		terrainScaleY = ((double)(jsEngine.get("terrainScaleY")));
		terrainScaleZ = ((double)(jsEngine.get("terrainScaleZ")));



		// build the world X, Y, Z axes to show origin
		x = new GameObject(GameObject.root(), linxS); 
 		y = new GameObject(GameObject.root(), linyS); 
  		z = new GameObject(GameObject.root(), linzS); 
  		(x.getRenderStates()).setColor(new Vector3f(1f,0f,0f)); 
  		(y.getRenderStates()).setColor(new Vector3f(0f,1f,0f)); 
  		(z.getRenderStates()).setColor(new Vector3f(0f,0f,1f)); 
		
		// build terrain
		terr = new GameObject(GameObject.root(), terrS, terrtx);
		initialTranslation = (new Matrix4f().translation((float)terrainLocX,(float)(terrainLocY),(float)(terrainLocZ)));
		terr.setLocalTranslation(initialTranslation);
		initialScale = (new Matrix4f().scaling((float)terrainScaleX,(float)terrainScaleY,(float)terrainScaleZ));
		terr.setLocalScale(initialScale);
		terr.setHeightMap(hills);

		// build avatar
		avatar = new GameObject(GameObject.root(), robotS, robottx);
		initialTranslation = (new Matrix4f()).translation((float)avatarPosX, (float)avatarPosY, (float)avatarPosZ);
		initialScale = (new Matrix4f()).scaling((float)avatarScale);	
		avatar.setLocalTranslation(initialTranslation);
		avatar.setLocalScale(initialScale);
		avatar.getRenderStates().setModelOrientationCorrection(
		(new Matrix4f()).rotationY((float)java.lang.Math.toRadians(180.0f)));

		// build sphere 
		sphere = new GameObject(GameObject.root(), sphereS, spheretx);
		initialTranslation = (new Matrix4f()).translation(
			setRandomLocation(), 
			1, 
			setRandomLocation());
		sphere.setLocalTranslation(initialTranslation); 
		initialScale = (new Matrix4f()).scaling(0.5f); 
  		sphere.setLocalScale(initialScale); 

		// build zombie
		zombie = new GameObject(GameObject.root(), zombieS, zombietx); 
		initialTranslation = (new Matrix4f()).translation(
			setRandomLocation(), 
			1, 
			setRandomLocation());
  		zombie.setLocalTranslation(initialTranslation); 
  		initialScale = (new Matrix4f()).scaling((float)zombieScale); 
  		zombie.setLocalScale(initialScale); 

		// Test Physics Objects
		grenade1 = new GameObject(GameObject.root(), grenadeS, grenadetx);
		grenade1.setLocalTranslation((new Matrix4f()).translation(0,4,0));
		grenade1.setLocalScale((new Matrix4f()).scaling(.35f));

		plane = new GameObject(GameObject.root(), planeS, spheretx);
		plane.setLocalTranslation((new Matrix4f()).translation(0, 0, 0));
		plane.setLocalScale((new Matrix4f()).scaling(.75f));

	}

	@Override
	public void initializeLights()
	{	
		// Ambient light
		Light.setGlobalAmbient(0.5f, 0.5f, 0.5f);
		// Positional light
		light1 = new Light();
		light1.setLocation(new Vector3f(5.0f, 4.0f, 2.0f));
		(engine.getSceneGraph()).addLight(light1);
	}

	@Override
	public void initializeGame()
	{	
		prevTime = System.currentTimeMillis();
		startTime = System.currentTimeMillis();

		// Initialize JavaScript engine
		ScriptEngineManager factory = new ScriptEngineManager();
		jsEngine = factory.getEngineByName("js");

		// Initialize parameters in InitParams.js
		scriptFile1 = new File ("assets/scripts/InitParams.js");
		this.runScript(scriptFile1);

		health = ((int)(jsEngine.get("health")));
		isAxesOn = ((boolean)(jsEngine.get("isAxesOn")));
		rotationSpeed = ((double)(jsEngine.get("rotationSpeed")));
		bounceSpeed = ((double)(jsEngine.get("bounceSpeed")));
	
	

		(engine.getRenderSystem()).setWindowDimensions(1900,1000);

		//---------------- Camera ----------------------
		cam = (engine.getRenderSystem()).getViewport("MAIN").getCamera();

		//---------------- Orbital Controller ------------
		orbitController = new CameraOrbit3D(cam, avatar, engine);

		//----------------- Node Controllers -------------------
		rc = new RotationController(engine, new Vector3f(0,1,0), (float)rotationSpeed);
		(engine.getSceneGraph()).addNodeController(rc); 
		bc = new BounceController(engine, (float)bounceSpeed);
		(engine.getSceneGraph()).addNodeController(bc);

		rc.toggle();
		bc.toggle();

		rc.addTarget(sphere);
		bc.addTarget(sphere);


		//--------------View Port---------------------
		(engine.getRenderSystem()).addViewport("VIEWPORT", 0.75f, 0f, 0.25f, 0.25f);	
		Viewport vp = (engine.getRenderSystem()).getViewport("VIEWPORT");
		vpCam = vp.getCamera();

		vp.setHasBorder(true);
		vp.setBorderWidth(4);
		vp.setBorderColor(0.0f, 1.0f, 0.0f);

		// Set viewport camera above avatar avatar
		vpCam.setLocation(new Vector3f(avatar.getWorldLocation().x(),
									avatar.getWorldLocation().y() + 10,
									avatar.getWorldLocation().z()));
		vpCam.setU(new Vector3f(1, 0, 0));
		vpCam.setV(new Vector3f(0, 0, -1));
		vpCam.setN(new Vector3f(0, -1, 0));
	
		//--------- INPUTS SECTION------------
		im = engine.getInputManager(); 
		
		setupNetworking();
		
		FwdAction fwdAction = new FwdAction(this, protClient);
		BwdAction bwdAction = new BwdAction(this, protClient);
		LeftAction leftAction = new LeftAction(this, protClient);
		RightAction rightAction = new RightAction(this, protClient);
		FwdBwdAction fwdBwdAction = new FwdBwdAction(this, protClient);
		TurnAction turnAction = new TurnAction(this, protClient);

		ToggleAxesAction toggleAxesAction = new ToggleAxesAction(this); 

		ViewportUpAction viewportUpAction = new ViewportUpAction(this);
		ViewportDownAction viewportDownAction = new ViewportDownAction(this);
		ViewportLeftAction viewportLeftAction = new ViewportLeftAction(this);
		ViewportRightAction viewportRightAction = new ViewportRightAction(this);
		ViewportZoomInAction viewportZoomInAction = new ViewportZoomInAction(this);
		ViewportZoomOutAction viewportZoomOutAction = new ViewportZoomOutAction(this);

		// Keyboard
		im.associateActionWithAllKeyboards( 
			net.java.games.input.Component.Identifier.Key._1, toggleAxesAction, 
			InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		im.associateActionWithAllKeyboards( 
			net.java.games.input.Component.Identifier.Key.W, fwdAction, 
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards( 
			net.java.games.input.Component.Identifier.Key.S, bwdAction, 
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(
			net.java.games.input.Component.Identifier.Key.A, leftAction,
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(
			net.java.games.input.Component.Identifier.Key.D, rightAction,
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

		im.associateActionWithAllKeyboards(
			net.java.games.input.Component.Identifier.Key.I, viewportUpAction,
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(
			net.java.games.input.Component.Identifier.Key.K, viewportDownAction,
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(
			net.java.games.input.Component.Identifier.Key.J, viewportLeftAction,
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(
			net.java.games.input.Component.Identifier.Key.L, viewportRightAction,
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);	
		im.associateActionWithAllKeyboards(
			net.java.games.input.Component.Identifier.Key._0, viewportZoomInAction,
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);	
		im.associateActionWithAllKeyboards(
			net.java.games.input.Component.Identifier.Key._9, viewportZoomOutAction,
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);	

		// Gamepad
		im.associateActionWithAllGamepads(
			net.java.games.input.Component.Identifier.Button._7, toggleAxesAction, 
			InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);	
		im.associateActionWithAllGamepads( 
			net.java.games.input.Component.Identifier.Axis.Y, fwdBwdAction, 
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllGamepads( 
			net.java.games.input.Component.Identifier.Axis.X, turnAction, 
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllGamepads( 
			net.java.games.input.Component.Identifier.Axis.Button._3, viewportUpAction, 
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllGamepads( 
			net.java.games.input.Component.Identifier.Axis.Button._0, viewportDownAction, 
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllGamepads( 
			net.java.games.input.Component.Identifier.Axis.Button._1, viewportRightAction, 
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllGamepads( 
			net.java.games.input.Component.Identifier.Axis.Button._2, viewportLeftAction, 
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllGamepads( 
			net.java.games.input.Component.Identifier.Axis.Button._4, viewportZoomOutAction, 
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllGamepads( 
			net.java.games.input.Component.Identifier.Axis.Button._5, viewportZoomInAction, 
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		
		// --Initialize Physics System--
		String engine = "tage.physics.JBullet.JBulletPhysicsEngine";
		float[] gravity = {0f,-5f,0f};
		physicsEngine = PhysicsEngineFactory.createPhysicsEngine(engine);
		physicsEngine.initSystem();
		physicsEngine.setGravity(gravity);
		
		// --Create Physics World--
		float mass = 1.0f;
		float up[] = {0,1,0};
		double[] tempTransform;

		Matrix4f translation = new Matrix4f(grenade1.getLocalTranslation());
		tempTransform = toDoubleArray(translation.get(vals));
		grenade1P = physicsEngine.addSphereObject(physicsEngine.nextUID(), mass, tempTransform, .35f);

		grenade1P.setBounciness(.2f);
		grenade1.setPhysicsObject(grenade1P);

		translation = new Matrix4f(plane.getLocalRotation());
		tempTransform = toDoubleArray(translation.get(vals));
		planeP = physicsEngine.addStaticPlaneObject(physicsEngine.nextUID(), tempTransform, up, 0.0f);
		
		planeP.setBounciness(1.0f);
		plane.setPhysicsObject(planeP);


		// Initialize Sound

		initAudio();
	}

	@Override
	public void update()
	{	
		// Calculate elapsed time
		elapsedTime = System.currentTimeMillis() - prevTime;
		prevTime = System.currentTimeMillis();
		deltaTime = elapsedTime * 0.03;
		
		// Update Physics
		if (running = true) 
		{
			Matrix4f mat = new Matrix4f();
			Matrix4f mat2 = new Matrix4f().identity();
			checkForCollisions();
			physicsEngine.update((float)elapsedTime);
			for (GameObject go:engine.getSceneGraph().getGameObjects()) 
			{
				if (go.getPhysicsObject() != null) 
				{
					mat.set(toFloatArray(go.getPhysicsObject().getTransform()));
					mat2.set(3,0,mat.m30());
					mat2.set(3,1,mat.m31());
					mat2.set(3,2,mat.m32());
					go.setLocalTranslation(mat2);
				} 
			}
		}

		//  Build Main HUD
		float mainRelativeLeft = engine.getRenderSystem().getViewport("MAIN").getRelativeLeft();
        float mainRelativeBottom = engine.getRenderSystem().getViewport("MAIN").getRelativeBottom();
        float mainActualWidth = engine.getRenderSystem().getViewport("MAIN").getActualWidth();
        float mainActualHeight = engine.getRenderSystem().getViewport("MAIN").getActualHeight();

		String healthCounterStr = Integer.toString(health);
		String dispStr1 = "Health = " + healthCounterStr;
		Vector3f hud1Color = new Vector3f(1,0,0);
		Vector3f hud2Color = new Vector3f(0,0,1);
		Vector3f hud3Color = new Vector3f(0,1,0);	
		(engine.getHUDmanager()).setHUD1(dispStr1, 
										hud1Color, 
										(int)(mainRelativeLeft * mainActualWidth + 10), 
										(int)(mainRelativeBottom * mainActualHeight + 10));

		// Build Viewport HUD
		float vpRelativeLeft = engine.getRenderSystem().getViewport("VIEWPORT").getRelativeLeft();
        float vpRelativeBottom = engine.getRenderSystem().getViewport("VIEWPORT").getRelativeBottom();
        float vpActualWidth = engine.getRenderSystem().getViewport("VIEWPORT").getActualWidth();
        float vpActualHeight = engine.getRenderSystem().getViewport("VIEWPORT").getActualHeight();

		String avatarPos = (int)avatar.getWorldLocation().x() + ", " +
						(int)avatar.getWorldLocation().y() + ", " +
						(int)avatar.getWorldLocation().z();				
		(engine.getHUDmanager()).setHUD2(avatarPos, 
										hud2Color,
										(int)(vpRelativeLeft * mainActualWidth + 10),
										(int)(vpRelativeBottom * vpActualHeight + 10));

			
		// Update altitude of avatar based on height map
		Vector3f loc = avatar.getWorldLocation();
		float height = terr.getHeight(loc.x(), loc.z());
		avatar.setLocalLocation(new Vector3f(loc.x(), height, loc.z()));

		im.update((float)elapsedTime);
		orbitController.updateCameraPosition();
		processNetworking((float)elapsedTime);
	}

	// Physics Engine Collision Check
	private void checkForCollisions(){ 
        com.bulletphysics.dynamics.DynamicsWorld dynamicsWorld;
        com.bulletphysics.collision.broadphase.Dispatcher dispatcher;
        com.bulletphysics.collision.narrowphase.PersistentManifold manifold;
        com.bulletphysics.dynamics.RigidBody object1, object2;
        com.bulletphysics.collision.narrowphase.ManifoldPoint contactPoint;
        dynamicsWorld = ((JBulletPhysicsEngine)physicsEngine).getDynamicsWorld();
        dispatcher = dynamicsWorld.getDispatcher();
        int manifoldCount = dispatcher.getNumManifolds();
        for (int i=0; i<manifoldCount; i++){ 
            manifold = dispatcher.getManifoldByIndexInternal(i);
            object1 = (com.bulletphysics.dynamics.RigidBody)manifold.getBody0();
            object2 = (com.bulletphysics.dynamics.RigidBody)manifold.getBody1();
            JBulletPhysicsObject obj1 = JBulletPhysicsObject.getJBulletPhysicsObject(object1);
            JBulletPhysicsObject obj2 = JBulletPhysicsObject.getJBulletPhysicsObject(object2);
            for (int j = 0; j < manifold.getNumContacts(); j++){ 
            contactPoint = manifold.getContactPoint(j);
                if (contactPoint.getDistance() < 0.0f){ 
                    //System.out.println("---- hit between " + obj1 + " and " + obj2);
					break;
				}
			}
		}


	// Update Sound
		zombieSound.setLocation(zombie.getWorldLocation());
		//ambientSound.setLocation(terr.getWorldLocation());
		setEarParameters();
	}



	// Generates random number between -10 and 10 for loc coords
	private int setRandomLocation(){
		Random r = new Random();
		int randomNum = r.nextInt(10 + 10) - 10;
		return randomNum;
	}

	// Toggles render for Line axes
	public void toggleAxes(){
		if(isAxesOn){
			x.getRenderStates().disableRendering();
			y.getRenderStates().disableRendering();
			z.getRenderStates().disableRendering();
			isAxesOn = false;
		}
		else{
			x.getRenderStates().enableRendering();
			y.getRenderStates().enableRendering();
			z.getRenderStates().enableRendering();
			isAxesOn = true;
		}
	}

	
	// --------- SCRIPTING -----------
	private void runScript(File scriptFile)
	{
		try
		{
			FileReader fileReader = new FileReader(scriptFile);
			jsEngine.eval(fileReader);
			fileReader.close();
		}
		catch(FileNotFoundException e1)
		{
			System.out.println(scriptFile + " not found " + e1); }
	    catch (IOException e2)
		{
           	System.out.println("IO problem with " + scriptFile + e2); 
		}
	    catch (ScriptException e3)
		{ 
           	System.out.println("ScriptException in " + scriptFile + e3); 
		}
	    catch (NullPointerException e4)
		{ 
            System.out.println ("Null ptr exception reading " + scriptFile + e4);
		}
	}

	// ---------- NETWORKING SECTION ----------------

	public ObjShape getGhostShape() { return ghostS; }
	public TextureImage getGhostTexture() { return ghostT; }
	public GhostManager getGhostManager() { return gm; }
	public Engine getEngine() { return engine; }
	
	private void setupNetworking()
	{	isClientConnected = false;	
		try 
		{	protClient = new ProtocolClient(InetAddress.getByName(serverAddress), serverPort, serverProtocol, this);
		} 	catch (UnknownHostException e) 
		{	e.printStackTrace();
		}	catch (IOException e) 
		{	e.printStackTrace();
		}
		if (protClient == null)
		{	System.out.println("missing protocol host");
		}
		else
		{	// Send the initial join message with a unique identifier for this client
			System.out.println("sending join message to protocol host");
			protClient.sendJoinMessage();
		}
	}
	
	protected void processNetworking(float elapsTime)
	{	// Process packets received by the client from the server
		if (protClient != null)
			protClient.processPackets();
	}

	public void setIsConnected(boolean value) { this.isClientConnected = value; }
	
	private class SendCloseConnectionPacketAction extends AbstractInputAction
	{	@Override
		public void performAction(float time, net.java.games.input.Event evt) 
		{	if(protClient != null && isClientConnected == true)
			{	protClient.sendByeMessage();
			}
		}
	}

	//---------------------- AUDIO --------------

	public void initAudio() {
		AudioResource resource1, resource2;
		audioMgr = AudioManagerFactory.createAudioManager("tage.audio.joal.JOALAudioManager");
		if (!audioMgr.initialize()) {
			System.out.println("Audio Manager failed to initialize!");
			return;
		}

		resource1 = audioMgr.createAudioResource("assets/sounds/zombie.wav", AudioResourceType.AUDIO_SAMPLE);
		resource2 = audioMgr.createAudioResource("assets/sounds/ambient.wav", AudioResourceType.AUDIO_STREAM);
		zombieSound = new Sound(resource1,SoundType.SOUND_EFFECT, 100, true);
		ambientSound = new Sound(resource2, SoundType.SOUND_EFFECT, 100, true);

		zombieSound.initialize(audioMgr);
		ambientSound.initialize(audioMgr);

		zombieSound.setMaxDistance(10.0f);
		zombieSound.setMinDistance(0.5f);
		zombieSound.setRollOff(5.0f);

		ambientSound.setMaxDistance(10.0f);
		ambientSound.setMinDistance(0.5f);
		ambientSound.setRollOff(5.0f);

		zombieSound.setLocation(zombie.getWorldLocation());
		ambientSound.setLocation(terr.getWorldLocation());

		setEarParameters();

		zombieSound.play();
		ambientSound.play();
		
	}

	public void setEarParameters() {
		Camera camera = (engine.getRenderSystem().getViewport("MAIN").getCamera());
		audioMgr.getEar().setLocation(avatar.getWorldLocation());
		audioMgr.getEar().setOrientation(camera.getN(), new Vector3f(0.0f, 1.0f, 0.0f));
	}

	//--------------------- GETTERS -------------

	public GameObject getAvatar(){return avatar;}

	public Camera getCamera(){
		return engine.getRenderSystem().getViewport("MAIN").getCamera();
	}
		
	public Camera getViewportCamera(){
		return engine.getRenderSystem().getViewport("VIEWPORT").getCamera();
	}
	
	public Vector3f getPlayerPosition() { 
		return avatar.getWorldLocation();
	}

	public double getDeltaTime(){
		return this.deltaTime;
	}

	// -------------PHYSICS UTILITY -------------
	public static float[] toFloatArray(double[] arr){ 
        if (arr == null) return null;
        int n = arr.length;
        float[] ret = new float[n];
        for (int i = 0; i < n; i++){ 
            ret[i] = (float)arr[i];
        }
        return ret;
    }

    public static double[] toDoubleArray(float[] arr){ 
        if (arr == null) return null;
        int n = arr.length;
        double[] ret = new double[n];
        for (int i = 0; i < n; i++){ 
            ret[i] = (double)arr[i];
        }
        return ret;
    }
}


				



