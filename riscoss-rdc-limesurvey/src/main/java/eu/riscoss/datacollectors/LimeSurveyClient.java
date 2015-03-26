package eu.riscoss.datacollectors;

import java.text.Format;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.IOException;

import javax.xml.bind.DatatypeConverter;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class LimeSurveyClient {
	private String username;
	private String password;
	private String url;

	private HttpClient client = HttpClientBuilder.create().build();

	/** 
	 * Constructor of LimeSurveyClients
	 * 
	 * @param username
	 *            username of the admin of LimeSurvey
	 * @param password
	 *            password of the admin of LimeSurvey
	 * @param url
	 *            root url where limeSurvey is installed
	 */
	public LimeSurveyClient(String username, String password, String url) {
		this.username = username;
		this.password = password;
		this.url = url;
	}

	protected String formatAnswer(String answer) {
		if (answer.trim().isEmpty())
			return "0";
		else {
			switch (answer.toUpperCase()) {
			case "ZERO":
			case "N": {
				return "0";

			}
			case "HALF": {
				return "0.5";
			}
			case "ONE":
			case "Y": {
				return "1";
			}
			case "TWO": {
				return "2";
			}
			case "TREE": {
				return "3";
			}
			case "FOUR": {
				return "4";
			}
			case "FIVE": {
				return "5";
			}
			case "SIX": {
				return "6";
			}
			case "SEVEN": {
				return "7";
			}
			case "EIGHT": {
				return "8";
			}
			case "NINE": {
				return "9";
			}
			case "TEN": {
				return "10";
			}

			}
		}
		return answer;

	}

	protected String formatQuestion(String question) {

		Pattern pattern = Pattern.compile("\\[(.*?)\\]");
		Matcher matcher = pattern.matcher(question);
		if (matcher.find()) {
			return matcher.group(1);
		}

		return question;
	}

	protected boolean isIgnoredKey(String key) {
		ArrayList<String> ignoredKeys = new ArrayList<String>();
		ignoredKeys.add("id");
		ignoredKeys.add("lastpage");
		ignoredKeys.add("submitdate");
		ignoredKeys.add("startlanguage");
		ignoredKeys.add("datestamp");
		ignoredKeys.add("startdate");
		return ignoredKeys.contains(key);
	}

	/**
	 * Retrieves the answers of responseID for the survey identified by surveyID
	 * 
	 * @param surveyID
	 *            identifier of the survey
	 * @param responseID
	 *            identifier of the response
	 * @return the answers of responseID for the survey identified by surveyID
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public Hashtable<String, String> getResponse(int surveyID, int responseID)
			throws ClientProtocolException, IOException {
		String sessionKey = getSessionKey();
		String responses = exportResponseByResponseID(sessionKey, surveyID,
				responseID);

		CSVParser csvParser = CSVParser.parse(responses, CSVFormat.RFC4180);
		List<CSVRecord> csvRecordsList = csvParser.getRecords();
		CSVRecord csvQuestions = csvRecordsList.get(0);
		CSVRecord csvAnswers = csvRecordsList.get(1);

		Iterator<String> iterQuestion = csvQuestions.iterator();
		Iterator<String> iterAnswer = csvAnswers.iterator();

		Hashtable<String, String> questionAnswerDictionary = new Hashtable<String, String>();
		while (iterQuestion.hasNext()) {
			String question = iterQuestion.next();
			String answer = iterAnswer.next();
			if (!isIgnoredKey(question))
				questionAnswerDictionary.put(formatQuestion(question),
						formatAnswer(answer));

		}
		return questionAnswerDictionary;
	}

	/**
	 * Auxiliary method. Here we parse a Json line
	 * 
	 * @param jsonLine
	 *            jsonLine to parse
	 * @return the real content (i.e. body) of the json response.
	 */
	private String parseJsonLine(String jsonLine) {
		JsonElement jelement = new JsonParser().parse(jsonLine);
		JsonObject jobject = jelement.getAsJsonObject();
		String result = jobject.get("result").toString();
		return result;
	}

	/**
	 * Auxiliary method. Here we login to LimeSurvey and get the session key
	 * 
	 * @param client
	 *            The HTTP Client
	 * @return The session key.
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	private String getSessionKey() throws ClientProtocolException, IOException {
		HttpPost post = new HttpPost(this.url
				+ "/index.php/admin/remotecontrol");
		post.setHeader("Content-type", "application/json");
		String getSessionKeyRequest = "{\"method\": \"get_session_key\", \"params\": {\"username\": \""
				+ this.username
				+ "\", \"password\": \""
				+ this.password
				+ "\" }, \"id\": 1}";
		post.setEntity(new StringEntity(getSessionKeyRequest));
		HttpResponse response = client.execute(post);
		if (response.getStatusLine().getStatusCode() == 200) {
			HttpEntity entity = response.getEntity();
			String sessionKey = parseJsonLine(EntityUtils.toString(entity));
			return sessionKey;
		} else {
			// something has gone wrong...
			return response.getStatusLine().toString();
		}
	}

	/**
	 * Auxilary method. Here we invoke the LimeSurvey API to retrieve the
	 * questions and answers in CSV of a specific response
	 * 
	 * @param client
	 *            The HTTP Client
	 * @param sessionKey
	 *            The sessionkey (should be obtained invoking the getSessionKey
	 *            method)
	 * @param surveyID
	 *            the identifier of the survey
	 * @param responseID
	 *            The responseID that identify the response we want.
	 * @return a CSV String containing the questions and the answers of the
	 *         response identified by responseID
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	private String exportResponseByResponseID(String sessionKey, int surveyID,
			int responseID) throws ClientProtocolException, IOException {
		HttpPost post = new HttpPost(this.url
				+ "/index.php/admin/remotecontrol");
		post.setHeader("Content-type", "application/json");

		String exportResponsesRequest = "{\"method\": \"export_responses\", \"params\": "
				+ "{\"sSessionKey\": "
				+ sessionKey
				+ ", "
				+ "\"iSurveyID\": \""
				+ surveyID
				+ "\" , "
				+ "\"sDocumentType\": \"csv\", "
				+ "\"sLanguageCode\": null, "
				+ "\"sCompletionStatus\": \"all\", "
				+ "\"sHeadingType\": \"code\", "
				+ "\"sResponseType\": \"short\", "
				+ "\"iFromResponseID\": \""
				+ responseID
				+ "\","
				+ "\"iToResponseID\": \""
				+ responseID
				+ "\"" + "}, \"id\": 1}";
		post.setEntity(new StringEntity(exportResponsesRequest));
		HttpResponse response = client.execute(post);
		if (response.getStatusLine().getStatusCode() == 200) {
			HttpEntity entity = response.getEntity();
			String responsesBase64 = parseJsonLine(EntityUtils.toString(entity));
			String responsesDecoded = new String(
					DatatypeConverter.parseBase64Binary(responsesBase64),
					"UTF-8");
			return responsesDecoded;
		} else {
			// something has gone wrong...
			return response.getStatusLine().toString();
		}
	}

	/**
	 * Main method. Just for testing purposes
	 * 
	 * @param args
	 *            .
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	

}
