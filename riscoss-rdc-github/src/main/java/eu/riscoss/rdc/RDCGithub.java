package eu.riscoss.rdc;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.DatatypeConverter;

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

import eu.riscoss.dataproviders.Distribution;
import eu.riscoss.dataproviders.RiskData;
import eu.riscoss.dataproviders.RiskDataType;
import eu.riscoss.dataproviders.RiskDataUtils;

public class RDCGithub implements RDC {
	
	static final String GITHUB_PREFIX = "github:repository-";
	
	static Map<String,String>		names = new HashMap<>(); //names published in the rdr 
	static Map<String,String>		keys = new HashMap<>();
	static Map<String,RDCParameter>	parameters = new HashMap<>();
	
	private HttpClient client = HttpClientBuilder.create().build();
	
	Map<String,String> values = new HashMap<>();
	
	static {
		//github-specific indicators (not hardcoded, depends on availability!)
		keys.put( "forks_count", "number" );//== network_count == forks
		keys.put( "open_issues_count", "number" ); //all open issues created since the start of the project
		keys.put( "stargazers_count", "number" );//these are the "STAR" on the github web interface, same than watchers_count!
		keys.put( "created_at", "date" );
		keys.put( "subscribers_count", "number" );//these are the "WATCH" on the github web interface!
		keys.put( "open_issues", "number" );
		keys.put( "watchers_count", "number" );//these are the "STAR" on the github web interface!
		keys.put( "size", "number" );
		keys.put( "has_wiki", "boolean" );
		keys.put( "updated_at", "date" );
		
		//keys.put( "license", "object" ); //OBJECT NOT IMPLEMENTED! --> boolean "has_license"
		//	github:repository-ci_link?		//to check!  
		//  github:repository-closed_issues? //not available
	}
	
	static {
		//github-specific indicators (not hardcoded, depends on availability!)
		for (Entry<String, String> entry : keys.entrySet()) {
			names.put( GITHUB_PREFIX + entry.getKey(), entry.getValue() );
		}
		
		//number of users that did commits
		names.put( GITHUB_PREFIX + "contributors", "number" );
		//sum of all the commits done
		names.put( GITHUB_PREFIX + "contributions_sum", "number" );
		
		//is a Travis CI file present?
		names.put( GITHUB_PREFIX + "ci_link", "boolean" );
		
		//issues currently open (in last year's issues)
		names.put( GITHUB_PREFIX + "issue-openratio", "number");
		//issues closed till now (in last year's issues)
		names.put( GITHUB_PREFIX + "issue-closedratio", "number");
		
		//milliseconds for closing an issue, in history order
		names.put( GITHUB_PREFIX + "issue-open-close-diff", "numberlist");
		 //average milliseconds for closing an issue
		names.put( GITHUB_PREFIX + "issue-open-close-diff-avg", "number"); 
		names.put( GITHUB_PREFIX + "issue-comments", "numberlist");
		//average number of comments per issue
		names.put( GITHUB_PREFIX + "issue-comments-avg", "number");
		
		//weekly commit count for the last 52 weeks
		names.put( GITHUB_PREFIX + "participation", "numberlist");
		//weekly commit count sum (= commits in the last year)
		names.put( GITHUB_PREFIX + "participation_sum", "number");
				
		//hardcoded indicators
		names.put( GITHUB_PREFIX + "has_license", "boolean" ); //OBJECT NOT IMPLEMENTED!
		//general indicators (hardcoded)
		names.put( "size", "number" );
		
		parameters.put( "repository", new RDCParameter( "repository", "Repository name", "RISCOSS/riscoss-analyser", null ) );
	}
	
	@Override
	public String getName() {
		return "Github";
	}
	
	@Override
	public Collection<RDCParameter> getParameterList() {
		return parameters.values();
	}
	
	@Override
	public void setParameter(String parName, String parValue) {
		values.put( parName, parValue );
	}
	
	@Override
	public Map<String, RiskData> getIndicators( String entity ) {
		Map<String,RiskData> values = new HashMap<>();
		
		try {
			JSONAware json;
			json = parse(getDataWithLicense());
			if (json!=null)
				parseJsonRepo( json, entity , values); //json.substring(json.indexOf( "{" ) ), entity , values);
			json = parse(getRepoData("/contributors"));
			if (json!=null)
				parseJsonContributors(json, entity , values);
			json = parse(getRepoData("/contents"));
			if (json!=null)
				parseJsonContent(json, entity , values);
			json = parse(getRepoData("/stats/participation"));
			if (json!=null)
				parseJsonParticipation(json, entity , values);
			json = parse(getRepoData("/issues?state=all"));
			if (json!=null)
				parseJsonIssues(json, entity , values);
			return values;
		} catch( Exception e ) {
			e.printStackTrace();
			throw new RuntimeException( e );
		}
	}
	
	private JSONAware parse(String json){
		JSONAware jv = null;
		if (json.startsWith("WARNING")){
			System.err.println(json); //error message - implement different handling if needed
		} else try {
			jv = (JSONAware) new JSONParser().parse( json );
		} catch (ParseException e) {
			e.printStackTrace();//TODO
		}
		return jv;
	}
	
	private void parseJsonContent( JSONAware jv, String entity, Map<String, RiskData> values ) {
		int hasTravis = 0;
		

		if( jv instanceof JSONArray ) {
			JSONArray ja = (JSONArray)jv;

			for (Object o : ja){

				JSONObject jo = (JSONObject)o;
				//System.out.println(jo.get("contributions"));
				String filename = jo.get("name").toString();
				if (filename.equals(".travis.yml")) {
					hasTravis = 1;
					break;
				}
			}
		}
		RiskData rd = new RiskData( GITHUB_PREFIX + "ci_link", entity, new Date(), RiskDataType.NUMBER, hasTravis );
		values.put( rd.getId(), rd );	
	}
	
	private void parseJsonIssues(JSONAware jv, String entity, Map<String, RiskData> values) {
		
		if( jv instanceof JSONArray ) {
			JSONArray ja = (JSONArray)jv;
			
			int closedissues = 0;
			int openissues = 0;
			
			ArrayList<Double> diffList = new ArrayList<Double>();//should be Long, but only Double is supported in the REST data 
			ArrayList<Double> numCommentsList = new ArrayList<Double>();//should be integer
			
			for (Object o : ja){
				if (o instanceof JSONObject){
					JSONObject jo = (JSONObject)o;
					//System.out.println("   issue state: "+(((JSONObject)jo).get("state")));
					String s = ((JSONObject)jo).get("state").toString();
					if (s.equals("open"))
						openissues++;
					else if (s.equals("closed"))
						closedissues++;
					Calendar closedDate = null;
					Calendar openedDate = null;
					
					String openedAt = (String)((JSONObject)jo).get("created_at");
					if (openedAt != null) {
						openedDate = DatatypeConverter.parseDateTime(openedAt);
						//System.out.println("open: "+openedDate.getTime());
						String closedAt = (String)((JSONObject)jo).get("closed_at");
						if (closedAt != null) {
							closedDate = DatatypeConverter.parseDateTime(closedAt);
							long diff = closedDate.getTimeInMillis()-openedDate.getTimeInMillis();
							diffList.add(new Double(diff));
							
							//System.out.println(closedDate.getTimeInMillis()-openedDate.getTimeInMillis());
						}
					}
					numCommentsList.add(new Double((Long)((JSONObject)jo).get("comments")));
				}
			}
			
			double sum = ja.size();
			//assert(sum == openissues + closedissues);  //??sure??
			System.out.println(openissues+"   openissues  "+closedissues+" "+sum);
			RiskData rd = new RiskData(GITHUB_PREFIX + "issue-closedratio", entity, new Date(), RiskDataType.NUMBER, closedissues/sum);
			values.put(rd.getId(), rd);
			rd = new RiskData(GITHUB_PREFIX + "issue-openratio", entity, new Date(), RiskDataType.NUMBER, openissues/sum);
			values.put(rd.getId(), rd);
			
			Distribution d = new  Distribution(diffList);
			rd = new RiskData(GITHUB_PREFIX + "issue-open-close-diff", entity, new Date(), RiskDataType.DISTRIBUTION, new Distribution(diffList));
			values.put(rd.getId(), rd);
			 //average milliseconds for closing an issue
			rd = new RiskData(GITHUB_PREFIX + "issue-open-close-diff-avg", entity, new Date(), RiskDataType.NUMBER, d.getAverage()); 
			values.put(rd.getId(), rd);
			
			d = new  Distribution(numCommentsList);
			rd = new RiskData(GITHUB_PREFIX + "issue-comments", entity, new Date(), RiskDataType.DISTRIBUTION, d);
			values.put(rd.getId(), rd);
			rd = new RiskData(GITHUB_PREFIX + "issue-comments-avg", entity, new Date(), RiskDataType.NUMBER, d.getAverage());
			values.put(rd.getId(), rd);
			
		}
	}
	
	private void parseJsonParticipation(JSONAware jv, String entity, Map<String, RiskData> values) {
		if (jv instanceof JSONObject) {
			JSONObject jo = (JSONObject) jv;
			if (jo.containsKey("all")) {
				// JSONArray ja = (JSONArray)jo.get("all"));
				ArrayList<Long> ll = (ArrayList<Long>) jo.get("all");
				ArrayList<Double> doublelist = new ArrayList<Double>();
				Long sum = 0L;
				for (Long l : ll) {
					doublelist.add(l.doubleValue());
					sum += l;
				}

				Distribution d = new Distribution();
				d.setValues(doublelist);
				//weekly commit count for the repository owner and everyone else, 52 weeks
				RiskData rd = new RiskData(GITHUB_PREFIX + "participation", entity, new Date(),	RiskDataType.DISTRIBUTION, d);
				values.put(rd.getId(), rd);
				rd = new RiskData(GITHUB_PREFIX + "participation_sum", entity, new Date(), RiskDataType.NUMBER, sum);
				values.put(rd.getId(), rd);
			}
		}
	}
	
	private void parseJsonContributors( JSONAware jv, String entity, Map<String, RiskData> values ) {
		int contributions = 0;
		int contributors = 0;
		
		if( jv instanceof JSONArray ) {
			JSONArray ja = (JSONArray)jv;

			contributors = ja.size();

			for (Object o : ja){
				JSONObject jo = (JSONObject)o;
				//System.out.println(jo.get("contributions"));
				contributions += Integer.parseInt(jo.get("contributions").toString());
			}
		}	

		//number of contributors (i.e. persons that did a commit)
		RiskData rd = new RiskData( GITHUB_PREFIX + "contributors", entity, new Date(), RiskDataType.NUMBER, contributors );
		values.put( rd.getId(), rd );
		//sum of all the commits done
		rd = new RiskData( GITHUB_PREFIX + "contributions_sum", entity, new Date(), RiskDataType.NUMBER, contributions );
		values.put( rd.getId(), rd );
	}
	
	private void parseJsonRepo( JSONAware jv, String entity, Map<String, RiskData> values ) {
		if( jv instanceof JSONObject ) {
			JSONObject jo = (JSONObject)jv;
			for( Object key : jo.keySet() ) {
				//System.out.println(key+" \t"+jo.get(key) );
				if( keys.keySet().contains( key.toString() ) && (jo.get( key )!=null)) {
					String value = jo.get( key ).toString();

					if( "number".equals( keys.get( key.toString() ) ) ) {
						try {
							double d = Double.parseDouble( value );
							RiskData rd = new RiskData( GITHUB_PREFIX + key.toString(), entity, new Date(), RiskDataType.NUMBER, d );
							values.put( rd.getId(), rd );

							//hard-coded size value
							if (key.toString().equals("size")){
								rd = new RiskData( "size", entity, new Date(), RiskDataType.NUMBER, d );
								values.put( rd.getId(), rd );
							}
						} 
						catch( Exception ex ) {
							ex.printStackTrace();
						}
					}
					else if( "boolean".equals( keys.get( key.toString() ) ) ) {
						try {
							boolean b = Boolean.parseBoolean( value );
							RiskData rd = new RiskData( GITHUB_PREFIX + key.toString(), entity, new Date(), RiskDataType.NUMBER, (b ? 1 : 0) );
							values.put( rd.getId(), rd );
						}
						catch( Exception ex ) {
							ex.printStackTrace();
						}
					}
					else if( "date".equals( keys.get( key.toString() ) ) ) {
						try {
							value = value.replaceAll( "T", " " );
							SimpleDateFormat formatter = new SimpleDateFormat( "yyyy-MM-dd H:m:s" );
							Date date = formatter.parse( value );
							RiskData rd = new RiskData( GITHUB_PREFIX + key.toString(), entity, new Date(), RiskDataType.NUMBER, date.getTime() );
							values.put( rd.getId(), rd );
						}
						catch( Exception ex ) {
							ex.printStackTrace();
						}
					}
					//object currently not implemented in the RDR
					//implementation: hardcoded, as boolean, adding "has_" (see below)
					//							else if( "object".equals( keys.get( key.toString() ) ) ) {
					//									RiskData rd = new RiskData( GITHUB_PREFIX + key.toString(), entity, new Date(), RiskDataType.NUMBER, 1 );
					//									values.put( rd.getId(), rd );							
					//							}
				}
				if (key.toString().equals("license")){
					RiskData rd;
					if (jo.get( key ) == null ) 
						rd = new RiskData( GITHUB_PREFIX + "has_license", entity, new Date(), RiskDataType.NUMBER, 0 );
					else
						rd = new RiskData( GITHUB_PREFIX + "has_license", entity, new Date(), RiskDataType.NUMBER, 1 );
					values.put( rd.getId(), rd );
				}
			}
		}		
	}

	@Override
	public Collection<String> getIndicatorNames() {
		return names.keySet();
	}
	
	String getRepoData(String request) throws org.apache.http.ParseException, IOException {
		String repository = values.get( "repository" );
		return getData(repository+request, "");
	}
	
	String getDataWithLicense() throws org.apache.http.ParseException, IOException {
		String repository = values.get( "repository" );
		return getData(repository, "application/vnd.github.drax-preview+json");//to enable license info
	}
	
	/**
	 * 
	 * @param request empty or "/.....", also with parameters, e.g. "/issues?state=all"
	 * @return received json string
	 * @throws org.apache.http.ParseException
	 * @throws IOException
	 */
	String getData(String request, String header) throws org.apache.http.ParseException, IOException {
		System.out.println("Request string: https://api.github.com/repos/"+  request);
		HttpGet get = new HttpGet( "https://api.github.com/repos/" +  request);
		if (header!=""){
			get.setHeader("Accept", header);
		}

		HttpResponse response = client.execute(get);
		
		if (response.getStatusLine().getStatusCode() == 200) {
			HttpEntity entity = response.getEntity();
			return EntityUtils.toString(entity);

		} else if (response.getStatusLine().getStatusCode() == 202) {
			return "WARNING 202 Accept: Computing....try again in some seconds.";
			
		} else {
			// something has gone wrong...
			return "WARNING "+ response.getStatusLine().getStatusCode() +"\n"+response.getStatusLine().toString();
		}	
	}
}
