
public class LearningScore {
	
	
	private boolean win; // true for win and false for lost
	private int numOfMoves;
	private double[] state;
	
	public LearningScore(){
		this.win = true;
		this.numOfMoves = 0;
		this.state = new double[8 * 8 * 9];		// changed this to match size of learning table
	}

	public boolean getWin() {
		return win;
	}

	public void setWin(boolean win) {
		this.win = win;
	}

	public int getNumOfMoves() {
		return numOfMoves;
	}

	public void incNumOfMoves() {
		this.numOfMoves++;
	}

	public double[] getState() {
		return state;
	}

	public void setState(double[] state) {
		this.state = state;
	}

}
