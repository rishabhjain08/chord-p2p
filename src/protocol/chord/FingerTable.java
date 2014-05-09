package protocol.chord;

import java.io.Serializable;

public class FingerTable implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public Finger[] fingers;
	
	class Finger implements Serializable
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public Identifier start;
		//open interval from [start, interval)
		public Identifier interval;
		public Node node;
	}

	public FingerTable (int fingerCount)
	{
		fingers = new Finger[fingerCount];
		for (int i = 0; i < fingerCount; i++)
			fingers[i] = new Finger();
	}
	
	public void print ()
	{
		System.out.println("START" + " \t" + "INTERVAL" + " \t" + "NODE");
		System.out.println("------------------------------------------");
		for (int i = 0; i < this.fingers.length; i++)
		{
			Finger finger = this.fingers[i];
			//TODO: debug
			System.out.println(finger.start.id + " \t" + "[" + finger.start.id + ", " + finger.interval.id + ")" + " \t" + finger.node.getIdentifier().id);
		}
	}
}
