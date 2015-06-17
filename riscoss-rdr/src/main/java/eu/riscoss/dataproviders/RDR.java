package eu.riscoss.dataproviders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class RDR
{
    private static final Gson gson = new Gson();

    /**
     * Creates the JSon element from risk data.
     * TODO puts all NaN (results of 0/0) to "0" since NaN is not accepted in JSon
     * @param riskData
     * @return
     */
    private static JsonElement getRiskDataJson(RiskData riskData)
    {
        JsonObject riskDataJson = new JsonObject();
        riskDataJson.addProperty("id", riskData.getId());
        riskDataJson.addProperty("target", riskData.getTarget());
        riskDataJson.addProperty("type", riskData.getType().toString());

        switch (riskData.getType()) {
            case NUMBER: {
            	Object o = riskData.getValue();
            	if( o instanceof Float ) {
            		riskDataJson.addProperty("value", ((Float) riskData.getValue()).isNaN()?0.0:(Float) riskData.getValue());
            	}
            	else if( o instanceof Double ) {
            		riskDataJson.addProperty("value", ((Double) riskData.getValue()).isNaN()?0.0:(Double) riskData.getValue());
            	}
            	else if( o instanceof Integer ) {
            		riskDataJson.addProperty("value", (Integer) riskData.getValue());
            	}
            }
                break;
            case EVIDENCE:
                Evidence evidence = (Evidence) riskData.getValue();
                double[] evidenceData = new double[2];
                evidenceData[0] = evidence.getPositive();
                evidenceData[1] = evidence.getNegative();
                JsonElement value = gson.toJsonTree(evidenceData);
                riskDataJson.add("value", value);
                break;
            case DISTRIBUTION:
                Distribution distribution = (Distribution) riskData.getValue();
                List<Double> distribList  = distribution.getValues();
                List<Double> noNaN = new ArrayList<Double>();
                for (Double n : distribList) {
					if (n.isNaN())
						noNaN.add(0.0);
					else
						noNaN.add(n);
				}
                value = gson.toJsonTree(noNaN).getAsJsonArray();
                riskDataJson.add("value", value);
                break;
        }

        return riskDataJson;
    }

    public static JsonElement getRiskDataJson(List<RiskData> riskData)
    {
        JsonArray jsonArray = new JsonArray();
        for (RiskData rd : riskData) {
            jsonArray.add(getRiskDataJson(rd));
        }

        return jsonArray;
    }

    public static void sendRiskData(String riskDataRepositoryURL, List<RiskData> riskData) throws ClientProtocolException, IOException
            
    {
        CloseableHttpClient client = HttpClientBuilder.create().build();

        JsonElement riskDataJson = getRiskDataJson(riskData);

        HttpPost post = new HttpPost(riskDataRepositoryURL);
        post.setHeader("Content-type", "application/json");
        
              
        post.setEntity(new StringEntity(riskDataJson.toString()));
       
        CloseableHttpResponse response = client.execute(post);
//        System.out.println(response);
    }
}
