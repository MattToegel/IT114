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
}
