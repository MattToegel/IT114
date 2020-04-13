import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ScoreState implements Serializable{
	List<Score> scores = new ArrayList<Score>();
	public void getTop5() {
		scores.sort((Score a, Score b)->a.compareTo(b.score));
		for(int i = 0; i < 5; i++) {
			Score score = scores.get(i);
			System.out.println(score.name + ": " + score.score);
		}
	}
	@Override
	public String toString() {
		return String.format("{\"ScoreState\": %s}", this.scores);
	}
}

class Score implements Serializable{
	public String name;
	public long score;
	public Score(String n, long s) {
		this.name = n;
		this.score = s;
	}
	@Override
	public String toString() {
		return String.format("{\"name\":\"%s\", \"score\":\"%s\"}", name, score);
	}
	public int compareTo(long score) {
		if(this.score == score) {
			return 0;
		}
		else if (this.score < score) {
			return -1;
		}
		else {
			return 1;
		}
	}
}
