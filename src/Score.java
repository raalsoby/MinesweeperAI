import java.util.ArrayList;

// Stores the score of a bot as it plays games

public class Score {
	private ArrayList<Integer> moves; // Stores how many moves were made in a
										// given game
	private ArrayList<Boolean> won; // Stores whether the bot won a given game
	private int games;

	public Score() {
		games = 0;
		moves = new ArrayList<Integer>();
		won = new ArrayList<Boolean>();
	}

	public int addGame(int movesAdd, boolean wonAdd) { // Add data from a game
		moves.add(movesAdd);
		won.add(wonAdd);
		games++;
		return games;
	}

	public ArrayList<Integer> getMoves() { // get Data for moves made
		return moves;
	}

	public int getMoves(int game) { // get Data for moves made in specific game
		return moves.get(game);
	}

	public ArrayList<Boolean> getWon() { // get Data for games won
		return won;
	}

	public boolean getWon(int game) { // get Data for games won in specific game
		return won.get(game);
	}

	public double getWinRate() { // get avg win rate
		int wins = 0;
		for (boolean s : won) {
			if (s) {
				wins++;
			}
		}
		if (won.size() > 0) {
			return wins / won.size();
		}
		else {
			return -1;
		}
	}

	public int getGames() {
		return games;
	}
}
