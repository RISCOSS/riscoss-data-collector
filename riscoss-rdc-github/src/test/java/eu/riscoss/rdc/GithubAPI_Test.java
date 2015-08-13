package eu.riscoss.rdc;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class GithubAPI_Test {
	
	static Map<String,String> values = new HashMap<>();
	
	private static HttpClient client = HttpClientBuilder.create().build();


	public static void main(String[] args) {
		
		values.put("repository", "RISCOSS/riscoss-analyser");
		
	
		
//		gitJSONGetter("/contributors");
		//gitJSONGetter("/contents");
		//gitJSONGetter("commit_activity");
		gitJSONGetter() ; //"collaborators");
	}
	
	/**
	 * GIT API Test method 
	 * @param req
	 * @return
	 */
	static JSONArray gitJSONGetter(){
		String json = "";
		//String repository = values.get("repository");
		
		String owner = "RISCOSS/";
		//String repo = "riscoss-analyser/";
		String repo = "riscoss-data-collector/";
		
		String r = owner+repo;
		
		String req;
		req = r + "commits";
		
		//NST: needs some time to be calculated. 1st time it returns Status 202!
		req = r + "stats/contributors"; //NST single contributors with weekly efforts: w:week, a:add, d:del, c:#commits
		
		//https://developer.github.com/v3/repos/statistics/#commit-activity
		req = r + "stats/commit_activity";//NST data per week (1y):  The days array is a group of commits per day, starting on Sunday.
		
		req = r + "collaborators"; //needs authorization! Error 401
		
		req = r + "events"; //committs and other events. Attention: max 10x30 events, max 90days!
		
		req = r + "issues?state=all"; //all issues. Of interest: state=open, closed, 
		
//		req = r + "stats/participation";//NST  weekly commit count
		
		//req = r + "stats/code_frequency";  //NST: week,  number of additions, number of deletions per week
		
		//HttpGet( "https://api.github.com/rate_limit");  //rate limit is 60 requests per hour!!
		/**
		 * TODO:
		 * participation: week list, analysis value
		 * issues open, issues closed today status
		 *  
		 */
	
		
		HttpGet get = new HttpGet( "https://api.github.com/repos/" + req);
		//get = new HttpGet( "https://api.github.com/rate_limit");
		
		//only for getting the License
		//get.setHeader("Accept", "application/vnd.github.drax-preview+json");

		HttpResponse response;
		try {
			response = client.execute(get);

			if (response.getStatusLine().getStatusCode() == 200) {
				HttpEntity entity = response.getEntity();
				json = EntityUtils.toString(entity);

			} else if (response.getStatusLine().getStatusCode() == 202) {
				System.err.println("WARNING 202 Accept: Computing....try again in some seconds.");
				return null;
			} else {
				// something has gone wrong...
				System.err.println(response.getStatusLine().getStatusCode());
				System.err.println(response.getStatusLine().toString());
				return null;
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		System.out.println("****JSON****\n"+json+"\n************");
		
		try {
			JSONAware jv = (JSONAware) new JSONParser().parse( json );
			
			if (jv instanceof JSONObject){
				JSONObject jo = (JSONObject)jv;
				System.out.println("JO: ");
				for (Object o : jo.entrySet()) {
					System.out.println("\t"+o);
				}
				
			}
			
			
			if( jv instanceof JSONArray ) {
				JSONArray ja = (JSONArray)jv;
				
				int size = ja.size();
				System.out.println("JA Size = "+size);
				for (Object o : ja){
					if (o instanceof JSONObject){
						JSONObject jo = (JSONObject)o;
						System.out.println("JA Object:");
						for (Object o2 : jo.entrySet()) {
							System.out.println("\t"+o2);
						}
					} else {
						System.out.println("JA Array: " + (JSONArray)o);
					}
				}
				return ja;
			}

		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
	

}
