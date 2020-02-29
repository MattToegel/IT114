import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.World;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;

public class WSWorld {
	public static boolean isRunning = false;
	private static boolean _isRunning = false;
	World world;
	public WSWorld() {
		if(world == null) {
			world = new World();
			// the floor
			Body floor = new Body();
			floor.addFixture(Geometry.createRectangle(50.0, 0.2));
			floor.setMass(MassType.INFINITE);
			floor.translate(0, -3);
			floor.setUserData(new Object());
			this.world.addBody(floor);
		}
	}
	public void addBox() {
		if(world == null) {
			return;
		}
		Body body = new Body();
		body.addFixture(
				Geometry.createRectangle(40, 40));
		body.translate(1.0, 0.0);
		body.setMass(MassType.NORMAL);
		world.addBody(body);
	}
	public void start() {
		if(_isRunning) {
			return;
		}
		Thread worldLoop = new Thread() {
			@Override
			public void run() {
				while(WSWorld.isRunning) {
					world.step(1);
					try {
						Thread.sleep(8);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};
		worldLoop.start();
	}
}
