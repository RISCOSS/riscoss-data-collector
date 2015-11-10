/*
   (C) Copyright 2013-2016 The RISCOSS Project Consortium
   
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

*/

/**
 * @author 	Mirko Morandini
**/

package eu.riscoss.rdc;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.DatatypeConverter;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import eu.riscoss.dataproviders.Distribution;
import eu.riscoss.dataproviders.RiskData;
import eu.riscoss.dataproviders.RiskDataType;

public class RDCGithub implements RDC {
	
	static final String GITHUB_PREFIX = "github:repository-";
	
	static Map<String,String>		names = new HashMap<>(); //names published in the rdr 
	static Map<String,String>		keys = new HashMap<>();
	static Map<String,RDCParameter>	parameters = new HashMap<>();
	
	private HttpClient client = HttpClientBuilder.create().build();
	
	Map<String,String> values = new HashMap<>();
	String repository = "";
	
	static {
		//github-specific indicators (not hardcoded, depends on availability!)
		keys.put( "forks_count", "number" );//== network_count == forks
		keys.put( "open_issues_count", "number" ); //all open issues created since the start of the project
		keys.put( "stargazers_count", "number" );//these are thimport eu.riscoss.dataproviders.RiskDataUtils;e "STAR" on the github web interface, same than watchers_count!
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
		
		names.put( GITHUB_PREFIX + "commits_per_contributor", "number" );
		
		//is a Travis CI file present?
		names.put( GITHUB_PREFIX + "ci_link", "boolean" );
		
		//issues currently open (in last year's issues)
		names.put( GITHUB_PREFIX + "issue-openratio", "number");
		//issues closed till now (in last year's issues)
		names.put( GITHUB_PREFIX + "issue-closedratio", "number");
		
		//days for closing an issue, in history order, last year
		names.put( GITHUB_PREFIX + "issue-open-close-diff", "numberlist");
		 //average days for closing an issue, last year
		names.put( GITHUB_PREFIX + "issue-open-close-diff-avg", "number"); 
		
		//pull requests last year (from the issues list)
		names.put( GITHUB_PREFIX + "pull-requests", "number");
		
		names.put( GITHUB_PREFIX + "issue-comments", "numberlist");
		//average number of comments per issue, last year
		names.put( GITHUB_PREFIX + "issue-comments-avg", "number");
		
		//weekly commit count for the last 52 weeks
		names.put( GITHUB_PREFIX + "participation", "numberlist");
		//weekly commit count sum (= commits in the last year)
		names.put( GITHUB_PREFIX + "participation_sum", "number");
				
		//hardcoded indicators
		names.put( GITHUB_PREFIX + "has_license", "boolean" ); //OBJECT NOT IMPLEMENTED!
		//general indicators (hardcoded)
		names.put( "size", "number" );
		
		//commit distributions
		names.put(GITHUB_PREFIX + "percent_contributors_did_99_percent_of_commits", "number");
		names.put(GITHUB_PREFIX + "percent_contributors_did_95_percent_of_commits", "number");
		names.put(GITHUB_PREFIX + "percent_contributors_did_90_percent_of_commits", "number");
		names.put(GITHUB_PREFIX + "percent_contributors_did_80_percent_of_commits", "number");
		names.put(GITHUB_PREFIX + "percent_contributors_did_50_percent_of_commits", "number");
		
		//age in years, calculated from the reopsitories' "created_at" field
		names.put(GITHUB_PREFIX + "repository_age_years", "number");
		
		parameters.put( "repository", new RDCParameter( "repository", "Repository name", "RISCOSS/riscoss-analyser", null ) );
		parameters.put( "unamepwd", new RDCParameter( "unamepwd", "LEAVE THIS FIELD EMPTY to use default authentication. Github username:pwd (unauthenticated: only ca. 6 runs per hour possible)", "uname:pwd", "" ) );
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
		Map<String,RiskData> retValues = new HashMap<>();
		
		repository = values.get( "repository" );
		if (!repository.startsWith("https://") && !(repository.startsWith("http://"))) //to make possible that also entering the whole https address is allowed
			repository="https://api.github.com/repos/"+repository;
//		try {
//			new URL(repository);
//		} catch (MalformedURLException e1) {
//			e1.printStackTrace();
//		}
		
		try {
			String data = getDataWithLicense();
			if (data != null && data.indexOf("WARNING ") == 0) {
				return retValues;
			}
			JSONAware json = parse(data);
			if (json!=null)
				parseJsonRepo( json, entity , retValues); //json.substring(json.indexOf( "{" ) ), entity , values);
			json = parsePaged("/contributors", 20, 0); //30 per page here
			if (json!=null)
				parseJsonContributors(json, entity , retValues);
			json = parse(getRepoData("/contents"));
			if (json!=null)
				parseJsonContent(json, entity , retValues);
			json = parse(getRepoData("/stats/participation"));
			if (json!=null)
				parseJsonParticipation(json, entity , retValues);
			json = parsePaged("/issues?state=all", 10, 1); //32 per page here, 1 = max 1 year old (creation)
			if (json!=null)
				parseJsonIssues(json, entity , retValues, 1);
			return retValues;
		} catch( Exception e ) {
			e.printStackTrace();
			throw new RuntimeException( e );  //TODO: how are exceptions handled server-side??
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
	
	/**
	 * For paginated requests
	 * @param request
	 * @param maxPages max pages in paginated requests
	 * @param created_at_years maximum timespan for the "created at" field (used e.g. for issues). 0: no timespan
	 * @return
	 */
	private JSONAware parsePaged(String request, int maxPages, int created_at_years){

		JSONArray jaComplete = new JSONArray();
		
		char divider = '?';
		if (request.contains("?"))
			divider='&';
		
		Calendar lastyear = Calendar.getInstance();//actual
		lastyear.set(Calendar.YEAR, lastyear.get(Calendar.YEAR)-created_at_years);
		
		try {
			for (int i=1;i<=maxPages;i++){
				
				String jsonPage = getData(repository+request+divider+"page="+i, "");

				if (jsonPage.startsWith("WARNING")){
					System.err.println(jsonPage); //error message - implement different handling if needed
				} else try {
					JSONAware jv = (JSONAware) new JSONParser().parse( jsonPage );
					if( jv instanceof JSONArray ) {
						JSONArray ja = (JSONArray)jv;
						if (ja.size() == 0)
							break;
						jaComplete.addAll(ja);
						//do not scan more years
						if (created_at_years > 0){
							Calendar openedDate;
							String openedAt = (String)((JSONObject)ja.get(ja.size()-1)).get("created_at");
							if (openedAt != null) {
								openedDate = DatatypeConverter.parseDateTime(openedAt);
								//System.out.println("scan: opening date: "+openedDate.get(Calendar.YEAR)+" "+openedDate.get(Calendar.MONTH));
								//System.out.println("scan: last    date: "+lastyear.get(Calendar.YEAR)+" "+lastyear.get(Calendar.MONTH));

								if (openedDate.compareTo(lastyear) < 0){
									System.out.println("BREAK");
									break;
								}
							}
						}		
							
					}
				} catch (ParseException e) {
					e.printStackTrace();//TODO
				}	
			}		
			
		} catch (org.apache.http.ParseException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return jaComplete;
	}
	
//	private JSONAware parse(String json, int maxpages){
//		JSONAware jv = null;
//		if (json.startsWith("WARNING")){
//			System.err.println(json); //error message - implement different handling if needed
//		} else try {
//			jv = (JSONAware) new JSONParser().parse( json );
//		} catch (ParseException e) {
//			e.printStackTrace();//TODO
//		}
//		return jv;
//	}
	
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
	
	private void parseJsonIssues(JSONAware jv, String entity, Map<String, RiskData> values, int created_at_years) {

		if( jv instanceof JSONArray ) {
			JSONArray ja = (JSONArray)jv;
			
			int closedissues = 0;
			int openissues = 0;
			int pullrequests = 0;
			
			ArrayList<Double> diffList = new ArrayList<Double>();//should be Long, but only Double is supported in the REST data 
			ArrayList<Double> numCommentsList = new ArrayList<Double>();//should be integer
			
			for (Object o : ja){
				if (o instanceof JSONObject){
					JSONObject jo = (JSONObject)o;
					//System.out.println("   issue state: "+(((JSONObject)jo).get("state")));
					
					if (jo.get("pull_request")!=null){
						pullrequests++;
						continue;
					}
							
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
						if (closedAt != null && !closedAt.equals("")) {
							closedDate = DatatypeConverter.parseDateTime(closedAt);
							//System.out.println("parse: opening date: "+openedDate.get(Calendar.YEAR)+" "+openedDate.get(Calendar.MONTH));

							Calendar calendar = Calendar.getInstance();//actual
							calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR)-created_at_years);
							
							if (openedDate.compareTo(calendar) < 0){
								break;
							}
							
							long diff = closedDate.getTimeInMillis()-openedDate.getTimeInMillis();
							double diffd = diff / 1000 / 60 / 60 / 24; //difference in days.
							
							diffList.add(diffd);
							
						}
					}
					numCommentsList.add(new Double((Long)((JSONObject)jo).get("comments")));
				}
			}
			
			double sum = ja.size();
			//assert(sum == openissues + closedissues);  //??sure??
			System.out.println(openissues+"   openissues  + "+closedissues+" closedissues = "+sum);
			RiskData rd = null;
			if( sum > 0 ) {
				rd = new RiskData(GITHUB_PREFIX + "issue-closedratio", entity, new Date(), RiskDataType.NUMBER, closedissues/sum);
				values.put(rd.getId(), rd);
				rd = new RiskData(GITHUB_PREFIX + "issue-openratio", entity, new Date(), RiskDataType.NUMBER, openissues/sum);
				values.put(rd.getId(), rd);
			}
			
			Distribution d = new  Distribution(diffList);
			 //days for closing issues
			rd = new RiskData(GITHUB_PREFIX + "issue-open-close-diff", entity, new Date(), RiskDataType.DISTRIBUTION, new Distribution(diffList));
			values.put(rd.getId(), rd);
			 //average days for closing an issue
			rd = new RiskData(GITHUB_PREFIX + "issue-open-close-diff-avg", entity, new Date(), RiskDataType.NUMBER, d.getAverage()); 
			values.put(rd.getId(), rd);
			
			rd = new RiskData(GITHUB_PREFIX + "pull-requests", entity, new Date(), RiskDataType.NUMBER, pullrequests); 
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
			//System.out.println("contributors: "+contributors);

			for (Object o : ja){
				JSONObject jo = (JSONObject)o;
				//System.out.println(jo.get("contributions"));
				contributions += Integer.parseInt(jo.get("contributions").toString());
			}
			
			getContribDistrib(ja, contributions, 99, entity, values);
			getContribDistrib(ja, contributions, 95, entity, values);
			getContribDistrib(ja, contributions, 90, entity, values);
			getContribDistrib(ja, contributions, 80, entity, values);
			getContribDistrib(ja, contributions, 50, entity, values);		

		}	

		//number of contributors (i.e. persons that did a commit)
		RiskData rd = new RiskData( GITHUB_PREFIX + "contributors", entity, new Date(), RiskDataType.NUMBER, contributors );
		values.put( rd.getId(), rd );
		//sum of all the commits done
		rd = new RiskData( GITHUB_PREFIX + "contributions_sum", entity, new Date(), RiskDataType.NUMBER, contributions );
		values.put( rd.getId(), rd );
		
		//commits per contributor
		if( contributors > 0 ) {
			rd = new RiskData( GITHUB_PREFIX + "commits_per_contributor", entity, new Date(), RiskDataType.NUMBER, contributions/contributors );
			values.put( rd.getId(), rd );
		}
	}
	
	//TODO: rewrite in a more efficient way, caching the data
	private void getContribDistrib(JSONArray ja, int contributions, int limit, String entity, Map<String, RiskData> values) {
		int currlimit = contributions * limit / 100; //truncating is ok
		int sum = 0;
		int num = 0;
		
		//contributors seem already to be sorted by number of contributions
		for (Object o : ja){
			JSONObject jo = (JSONObject)o;
			sum += Integer.parseInt(jo.get("contributions").toString());
			num++;
			if (sum>=currlimit)
				break;
		}
		String idName = GITHUB_PREFIX + "percent_contributors_did_"+limit+"_percent_of_commits";
		if( ja.size() > 0 ) {
			RiskData rd = new RiskData(idName, entity, new Date(), RiskDataType.NUMBER, (double)num/ja.size() );
			values.put( rd.getId(), rd );
		}
		//System.out.println("with limit "+limit+"% : "+(double)num/ja.size());
	}

	private void parseJsonRepo( JSONAware jv, String entity, Map<String, RiskData> values ) {
		final long MILLISEC_YEAR = 365L*24*3600*1000;
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
							
							//calculate also the repository age!
							if(key.toString().equals("created_at")){ 
								long datediff = new Date().getTime() - date.getTime();
								double years = (double) datediff / MILLISEC_YEAR;
								rd = new RiskData( GITHUB_PREFIX + "repository_age_years", entity, new Date(), RiskDataType.NUMBER, years);
								values.put( rd.getId(), rd );	
							}
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
		//String repository = values.get( "repository" );
		return getData(repository+request, "");
	}
	
	String getDataWithLicense() throws org.apache.http.ParseException, IOException {
		//String repository = values.get( "repository" );
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
		HttpGet get = new HttpGet( request ); //"https://api.github.com/repos/" +  request);
		if (header!="")
			get.setHeader("Accept", header);
		
		String unamepwd = values.get( "unamepwd" );
		
		String encoded;
		if (unamepwd!=null && !unamepwd.equals(""))
			encoded =  new String( Base64.encodeBase64(unamepwd.getBytes()));
		else
			encoded = "UmlzY29zc1VzZXI6UmlzY29zczIwMTU="; //standard RiscossUser (delete in final version)
		
		get.setHeader("Authorization", "Basic " + encoded); 
		HttpResponse response = client.execute(get);//WARNING 401 if not authorized
		System.out.println("response: "+response.toString());
		
		if (response.getStatusLine().getStatusCode() == 200) {
			HttpEntity entity = response.getEntity();
			return EntityUtils.toString(entity);

		} else if (response.getStatusLine().getStatusCode() == 202) {
			return "WARNING 202 Accept: Computing....try again in some seconds.";
			
		} else {
			// something has gone wrong... e.g. WARNING 401 if Unauthorized
			return "WARNING "+ response.getStatusLine().getStatusCode() +"\n"+response.getStatusLine().toString();
		}	
	}
}
