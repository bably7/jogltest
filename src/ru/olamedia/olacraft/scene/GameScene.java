package ru.olamedia.olacraft.scene;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.HashMap;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

import org.ode4j.ode.DBody;

import ru.olamedia.Options;
import ru.olamedia.game.GameTime;
import ru.olamedia.liveEntity.LiveEntity;
import ru.olamedia.olacraft.game.Game;
import ru.olamedia.olacraft.physics.GamePhysicsWorld;
import ru.olamedia.olacraft.render.jogl.ChunkRenderer;
import ru.olamedia.olacraft.render.jogl.InventoryRenderer;
import ru.olamedia.olacraft.render.jogl.joglViewport;
import ru.olamedia.olacraft.weapon.Bullet;
import ru.olamedia.olacraft.weapon.BulletScene;
import ru.olamedia.olacraft.world.block.Block;
import ru.olamedia.olacraft.world.blockTypes.AbstractBlockType;
import ru.olamedia.olacraft.world.blockTypes.DirtBlockType;
import ru.olamedia.olacraft.world.blockTypes.GrassBlockType;
import ru.olamedia.olacraft.world.blockTypes.GravelBlockType;
import ru.olamedia.olacraft.world.blockTypes.StoneBlockType;
import ru.olamedia.olacraft.world.chunk.BlockSlice;
import ru.olamedia.olacraft.world.chunk.Chunk;
import ru.olamedia.olacraft.world.chunk.ChunkSlice;
import ru.olamedia.olacraft.world.location.BlockLocation;
import ru.olamedia.olacraft.world.provider.WorldProvider;
import ru.olamedia.player.Player;

public class GameScene {

	private HashMap<Integer, LiveEntity> liveEntities = new HashMap<Integer, LiveEntity>();
	WorldProvider provider;
	private int renderDistance = Options.renderDistance;
	private joglViewport viewport;
	private BulletScene bullets = new BulletScene();
	private GamePhysicsWorld physics = new GamePhysicsWorld();

	private boolean isInitialized = false;
	BlockSlice viewSlice;
	public GameTime time = new GameTime();

	public GameScene(WorldProvider provider) {
		this.provider = provider;
		setRenderDistance(renderDistance);
	}

	public void addBullet(Bullet b) {
		bullets.add(b);
		DBody body = physics.createBody();
		body.setPosition(b.location.x, b.location.y, b.location.z);
		body.setLinearVel(b.velocity.x, b.velocity.y, b.velocity.z);
		/*
		 * DMass mass = OdeHelper.createMass();
		 * mass.setMass(10);
		 * mass.setI(OdeHelper.c);
		 * body.setMass(mass);
		 */
		b.body = body;
	}

	public int getBulletsCount() {
		return bullets.getCount();
	}

	public void init(GLAutoDrawable drawable) {
		if (isInitialized) {
			return;
		}
		isInitialized = true;
		time.init();
		registerTextures();
		viewport = new joglViewport(drawable);
	}

	private void registerTextures() {
		AbstractBlockType t;
		t = new GrassBlockType();
		t.register(provider);
		t = new StoneBlockType();
		t.register(provider);
		t = new DirtBlockType();
		t.register(provider);
		t = new GravelBlockType();
		t.register(provider);
	}

	/**
	 * @return the renderDistance
	 */
	public int getRenderDistance() {
		return renderDistance;
	}

	/**
	 * @param renderDistance
	 *            the renderDistance to set
	 */
	public void setRenderDistance(int renderDistance) {
		this.renderDistance = renderDistance;
		viewSlice = new BlockSlice(provider, renderDistance, renderDistance, renderDistance);
		blockRenderer = new ChunkRenderer(viewSlice);
	}

	public ChunkRenderer blockRenderer = new ChunkRenderer(viewSlice);
	GLU glu = new GLU();

	public void registerLiveEntity(LiveEntity entity) {
		// liveEntityIncrement++;
		// entity.setId(liveEntityIncrement);
		liveEntities.put(entity.getConnectionId(), entity);
	}

	private InventoryRenderer inventoryRenderer;

	private Player player;

	public void registerPlayer(LiveEntity player) {
		inventoryRenderer = new InventoryRenderer(player.getInventory());
		this.player = (Player) player;
	}

	public LiveEntity getLiveEntity(int connectionId) {
		if (liveEntities.containsKey(connectionId)) {
			return liveEntities.get(connectionId);
		}
		return null;
	}

	public HashMap<Integer, LiveEntity> getLiveEntities() {
		return liveEntities;
	}

	public Block nearestBlock = null;

	public void tick() {
		time.tick();
		Game.instance.tick();
		float aspect = Game.Display.getAspect();
		Game.instance.camera.setAspect(aspect);
		// bullets.update(Game.instance.getDelta());
		physics.getWorld().step(Game.instance.getDelta());
		BlockSlice pickSlice = new BlockSlice(provider, 10, 10, 10);
		if (null != player) {
			if (player.isOrientationChanged || Game.instance.camera.isOrientationChanged) {
				player.isOrientationChanged = false;
				Game.instance.camera.isOrientationChanged = false;
				pickSlice.setCenter(player.getCameraX(), player.getCameraY(), player.getCameraZ());
				nearestBlock = pickSlice.getNearest(Game.instance.camera);
			}
		}
	}

	public void render(GLAutoDrawable drawable) {
		if (null == player || !Game.instance.isRunning()) {
			// not running, just clear screen
			GL2 gl = drawable.getGL().getGL2();
			gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
			gl.glClearColor(49f / 255f, 49f / 255f, 49f / 255f, 1);
			return;
		}
		init(drawable);
		float[] clearColor = time.getClearColor();
		GL2 gl = drawable.getGL().getGL2();
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		gl.glClearColor(clearColor[0], clearColor[1], clearColor[2], 1);
		// gl.glClearColor(49f / 255f, 119f / 255f, 243f / 255f, 1);
		// GOING 3D
		gl.glPushMatrix();
		gl.glPushAttrib(GL2.GL_ALL_ATTRIB_BITS);
		Game.instance.camera.setUp(drawable);
		// RENDER SUN
		// gl.glPushAttrib(GL2.GL_ALL_ATTRIB_BITS);
		// gl.glEnable(GL2.GL_BLEND); // Enable Blending
		// gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE); // Set Blending Mode To
		// // Mix Based On SRC
		// // Alpha
		// GLUquadric sun = glu.gluNewQuadric();
		// glu.gluQuadricDrawStyle(sun, GLU.GLU_FILL);
		// glu.gluQuadricNormals(sun, GLU.GLU_SMOOTH);
		// gl.glPushMatrix();
		// gl.glTranslatef(time.sun.getX() + player.getCameraX(),
		// time.sun.getY(), time.sun.getZ() + player.getCameraZ());
		// gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
		// gl.glColor4f(1, 1, 1, 0.02f);
		// gl.glColor4f((float) 251 / 255, (float) 255 / 255, (float) 228 / 255,
		// 0.02f);
		// glu.gluSphere(sun, 100f, 10, 10);
		// glu.gluSphere(sun, 90f, 10, 10);
		// glu.gluSphere(sun, 80f, 10, 10);
		// glu.gluSphere(sun, 70f, 10, 10);
		// glu.gluSphere(sun, 60f, 10, 10);
		// glu.gluSphere(sun, 50f, 10, 10);
		// glu.gluSphere(sun, 40f, 10, 10);
		// glu.gluSphere(sun, 35f, 10, 10);
		// gl.glColor4f(1, 1, 1, 1f);
		// glu.gluSphere(sun, 30f, 10, 10);
		// gl.glPopMatrix();
		// gl.glPopAttrib();

		viewSlice.setCenter((int) Game.instance.camera.getX(), (int) Game.instance.camera.getY(),
				(int) Game.instance.camera.getZ());
		// RENDER BLOCKS
		gl.glPopAttrib();
		gl.glPushAttrib(GL2.GL_ALL_ATTRIB_BITS);
		gl.glColor4f(0f, 1f, 0, 1);
		gl.glEnable(GL2.GL_DEPTH_TEST);
		gl.glShadeModel(GL2.GL_FLAT);
		gl.glCullFace(GL2.GL_BACK);
		// gl.glEnable(GL2.GL_FOG);
		// gl.glFogf(GL2.GL_FOG_MODE, GL2.GL_LINEAR);
		// gl.glFogf(GL2.GL_FOG_MODE, GL2.GL_EXP);
		// gl.glFogf(GL2.GL_FOG_START, renderDistance / 2 - renderDistance /
		// 10);
		// gl.glFogf(GL2.GL_FOG_END, renderDistance / 2);
		// gl.glFogf(GL2.GL_FOG_DENSITY, 0.002f);
		// new float[] { 49f / 255f, 119f / 255f, 243f / 255f }
		// gl.glFogfv(GL2.GL_FOG_COLOR, new float[] { 1, 1, 1, 0.2f }, 0);
		blockRenderer.render();
		gl.glPopAttrib();
		// RENDER ANYTHING ELSE
		gl.glPushAttrib(GL2.GL_ALL_ATTRIB_BITS);
		GLUquadric qobj0 = glu.gluNewQuadric();
		glu.gluQuadricDrawStyle(qobj0, GLU.GLU_FILL);
		glu.gluQuadricNormals(qobj0, GLU.GLU_SMOOTH);
		for (LiveEntity entity : liveEntities.values()) {
			gl.glPushMatrix();
			gl.glTranslatef(entity.getX(), entity.getCameraY(), entity.getZ());
			gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
			glu.gluSphere(qobj0, 0.1f, 10, 10);
			gl.glPopMatrix();
		}
		gl.glPopAttrib();
		if (nearestBlock != null) {
			gl.glPushAttrib(GL2.GL_ALL_ATTRIB_BITS);
			nearestBlock.renderFrame();
			gl.glPopAttrib();
		}
		// bullets.render(drawable);
		gl.glPopMatrix();

		// testObject.render();

		// GOIND 2D
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		int width = Game.Display.getWidth();
		int height = Game.Display.getHeight();
		glu.gluOrtho2D(0, width, height, 0);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glPushMatrix();
		// renderHUD();
		// MAP++
		gl.glPushAttrib(GL2.GL_ALL_ATTRIB_BITS);
		gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
		int msz = 100;
		gl.glColor4f(0.3f, 0.3f, 0.3f, 0.7f);
		gl.glRectf(width - msz - 12, 8, width - 8, msz + 12);
		gl.glColor4f(0.9f, 0.9f, 0.9f, 1);
		gl.glRectf(width - msz - 10, 10, width - 10, msz + 10);
		gl.glColor4f(0.0f, 0.0f, 0.0f, 0.9f);
		/*
		 * for (int mx = 0; mx < msz; mx++) {
		 * for (int mz = 0; mz < msz; mz++) {
		 * float h = (float) viewSlice
		 * .getHighest((int) (mx - msz / 2 + player.getX()), (int) (mz - msz / 2
		 * + player.getZ()));
		 * gl.glColor4f(h / 128, h / 128, h / 128, 1f);
		 * gl.glRectf(width - msz - 10 + mx, 10 + mz, width - msz - 10 + mx + 1,
		 * 10 + mz + 1);
		 * }
		 * }
		 */
		// MAP--
		// crosshair
		gl.glColor4f(1f, 1f, 1f, 0.7f);
		gl.glRectf(width / 2 - 1, height / 2 - 10, width / 2 + 1, height / 2 + 10); // vertical
		gl.glRectf(width / 2 - 10, height / 2 - 1, width / 2 + 10, height / 2 + 1); // horizontal

		// inventoryprivate PMVMatrix matrix;
		if (null != inventoryRenderer) {
			// inventoryRenderer.render(drawable);
		}

		viewport.drawText("rad: " + Options.renderDistance, 10, height - 20);
		viewport.drawText("avg fps: " + (int) Game.timer.getAvgFps(), 10, height - 35);
		viewport.drawText("fps: " + (int) Game.timer.getFps(), 10, height - 50);
		MemoryUsage heap = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
		viewport.drawText("mem: " + (heap.getUsed() / (1024 * 1024)) + "/" + (heap.getMax() / (1024 * 1024)), 10,
				height - 65);
		//
		// viewport.drawText("y: " + Game.instance.player.getY(), width - msz -
		// 10, height - msz - 20);
		// viewport.drawText("y: " + Game.instance.player.getCameraY() +
		// " (cam)", width - msz - 10, height - msz - 30);
		// viewport.drawText("x: " + Game.instance.player.getX(), width - msz -
		// 10, height - msz - 40);
		// viewport.drawText("z: " + Game.instance.player.getZ(), width - msz -
		// 10, height - msz - 50);
		// viewport.drawText("players: " + liveEntities.size(), width - msz -
		// 10, height - msz - 70);
		// viewport.drawText("bullets: " + getBulletsCount(), width - msz - 10,
		// height - msz - 95);
		// viewport.drawText("y velocity: " + player.velocity.y + " y accel: " +
		// player.acceleration.y + " inJump: "
		// + player.inJump + " onGround: " + player.onGround, width - msz - 350
		// - 10, height - msz - 110);
		// viewport.drawText("rdistance: " + Options.renderDistance, width - msz
		// - 10, height - msz - 155);

		// ChunkSlice cs = viewSlice.getChunkSlice();
		// viewport.drawText("slice x: " + cs.getX() + ".." + (cs.getX() +
		// cs.getWidth() - 1) + " y: " + cs.getY() + ".."
		// + (cs.getY() + cs.getHeight() - 1) + " z: " + cs.getZ() + ".." +
		// (cs.getZ() + cs.getDepth() - 1), width
		// - msz * 2 - 10, height - msz - 170);
		// viewport.drawText("time: " + time.getDateTimeString(), width - msz *
		// 2 - 10, height - msz - 185);
		// if (nearestBlock != null) {
		// viewport.drawText("pick: " + nearestBlock.getX() + "," +
		// nearestBlock.getY() + "," + nearestBlock.getZ()
		// + " d " + nearestBlock.getDistance(Game.instance.camera), width - msz
		// * 2 - 10, height - msz - 200);
		// viewport.drawText(
		// "edge: " + nearestBlock.location.isChunkEdge() + " left " +
		// nearestBlock.location.isChunkLeftEdge(),
		// width - msz * 2 - 10, height - msz - 210);
		// }

		// BlockLocation camloc = player.getCameraBlockLocation();
		// viewport.drawText("cam chunk: " + camloc.getChunkLocation(), width -
		// 200 - msz * 2 - 10, height - msz - 225);
		// Chunk camc =
		// viewSlice.getChunkSlice().getChunk(camloc.getChunkLocation());
		// viewport.drawText("cam chunk: " + camc.isMeshCostructed + " mesh: " +
		// camc.mesh, width - 200 - msz * 2 - 10,
		// height - msz - 235);
		// viewport.drawText(
		// "chunk available: " + camc.isAvailable() + " chunk empty: "
		// + (camc.isAvailable() ? camc.isEmpty() : "unknown"), width - 200 -
		// msz * 2 - 10, height - msz
		// - 245);

		gl.glPopAttrib();
		gl.glPopMatrix();
		// gl.glFlush();
	}

	public Player getPlayer() {
		return player;
	}
}
