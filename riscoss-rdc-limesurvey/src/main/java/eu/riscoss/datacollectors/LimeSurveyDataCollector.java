package eu.riscoss.datacollectors;

import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import org.apache.commons.io.IOUtils;

public class LimeSurveyDataCollector {
	private static final String COLLECTOR_ID = "LimeSurveyCollector";
	private static final String COLLECTOR_DATATYPE = "NUMBER";

	private static JSONObject testInput() {
		JSONObject input = null;
		try {
			input = new JSONObject("{}");
			input.put("username", "riscoss");
			input.put("url", "http://limesurvey.merit.unu.edu");
			input.put("surveyID", "584477");
			input.put("responseID", "20");
			input.put("targetEntity", "test");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return input;
	}

	protected static String formatType(String value) {
		String type = "NUMBER";
		try {
			Double.valueOf(value);

		} catch (NumberFormatException e) {
			type = "STRING";
		}

		return type;

	}


	public static void main(String[] args) throws Exception {
		JSONObject input;
		if (args.length > 0 && "--stdin-conf".equals(args[args.length - 1])) {
		
			String stdin = IOUtils.toString(System.in, "UTF-8");
			input = new JSONObject(stdin);
		} else {
			input = testInput();
			System.out.println("using " + input + " as test configuration.");
			System.out
					.println("In production, use --stdin-conf and pass configuration to stdin");
		}

		LimeSurveyClient limeSurveyClient = new LimeSurveyClient(
				input.getString("username"), input.getString("password"),
				input.getString("url"));

		Hashtable<String, String> questionAnswers = limeSurveyClient
				.getResponse(input.getInt("surveyID"),
						input.getInt("responseID"));

		String entity = input.getString("targetEntity");
		String value;
		JSONArray outArray = new JSONArray();

		for (String key : questionAnswers.keySet()) {
				JSONObject outObj = new JSONObject();
				outObj.put("id", key.toString());
				outObj.put("target", entity);
				value=String.valueOf(questionAnswers.get(key));
				outObj.put("value",value);
				outObj.put("type", formatType(value));
				outArray.put(outObj);
			
		}

		System.out.println("-----BEGIN RISK DATA-----");
		System.out.println(outArray.toString());
		System.out.println("-----END RISK DATA-----");
	}

}