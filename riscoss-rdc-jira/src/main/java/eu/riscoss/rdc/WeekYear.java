package eu.riscoss.rdc;


public class WeekYear implements Comparable<WeekYear> {
	int week;
	int year;
	public WeekYear (int week, int year) {
		this.week=week;
		this.year=year;
	}
	public int compareTo(WeekYear o) {
		if (year == o.year)
			return week-o.week;
		else
			return year-o.year;
	}
}
