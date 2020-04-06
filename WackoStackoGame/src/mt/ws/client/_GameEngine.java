package mt.ws.client;

public abstract class _GameEngine extends Thread{
	protected static boolean isRunning = false;
	public _GameEngine() {
		Awake();
		_GameEngine.isRunning = true;
	}
	/***
	 * Triggers once when constructor is called
	 */
	protected abstract void Awake();
	/***
	 * Triggers once when the game loop is started
	 */
	protected abstract void OnStart();
	/***
	 * This is our game loop handler
	 * Don't add logic here, just use the functions
	 */
	@Override
	public void run() {
		OnStart();
		while(isRunning) {
			Update();
			UILoop();
			try {
				Thread.sleep(16);//60 FPS
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		End();
	}
	/***
	 * Updates each frame (16ms)
	 */
	protected abstract void Update();
	protected abstract void UILoop();
	/***
	 * Triggers once when the game loop terminates
	 */
	protected abstract void End();
	
	public static float lerp(float a, float b, float f)
	{
	    return a + f * (b - a);
	}
	public static double distance(
			  double x1, 
			  double y1, 
			  double x2, 
			  double y2) {       
	    return Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
	}
}
