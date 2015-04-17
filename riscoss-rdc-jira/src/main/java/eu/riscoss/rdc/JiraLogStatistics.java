package eu.riscoss.rdc;

import java.util.ArrayList;
import java.util.TreeMap;

public class JiraLogStatistics {
	public int openBugs;
	public double timeToResolveABug;
	public double timeToResolveABlockingOrCriticalBug;
	public int numberOfFeatureRequests;
	public int numberOfOpenFeatureRequests;
	public double numberOfClosedFeatureRequestsPerUpdate;
	public double numberOfClosedBugsPerUpdate;
	public boolean presenceOfBugsCorrected;
	public int numberOfSecurityBugs;
	public double timeToResolveASecurityBug;
	public int counterSecurityBugs;
	public double totalSecurityBugFixTime;
	public int counterCriticalBugs;
	public double totalCriticalBugFixTime;
	public int counterCloseBugs;
	public double totalBugFixTime;
	public int totalBugs;
	public ArrayList<Double> list_bugFixTime = new ArrayList<Double>(); //bug fix times in days
//	public ArrayList<Double> distribution_bugFixTime = new ArrayList<Double>(); //bug fix times in days, divided by ........
	public ArrayList<Double> list_bugFixTimeCriticalBlocker = new ArrayList<Double>(); //critical bug fix times in days
	public ArrayList<String> list_bugFixDayOfWeek = new ArrayList<String>(); //weekdays for closing bug (1:Mon,.., 7:Sun)
	public ArrayList<String> list_GeneralFixDayOfWeek = new ArrayList<String>(); //weekdays for closing bug or feature (1:Mon,.., 7:Sun)
	public TreeMap<WeekYear, Integer> list_commit_frequency_week = new TreeMap<WeekYear,Integer>(); //critical bug fix times in days
	public ArrayList<Integer> list_commit_hour = new ArrayList<Integer>();
}
