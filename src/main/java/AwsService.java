import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.appconfig.AmazonAppConfig;
import com.amazonaws.services.appconfig.AmazonAppConfigClient;
import com.amazonaws.services.appconfig.model.GetConfigurationRequest;
import com.amazonaws.services.appconfig.model.GetConfigurationResult;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public final class AwsService {

    private final String awsAccessKey;
    private final String awsSecretKey;

    private final String application;
    private final String environment;
    private final String config;
    private final String clientId;

    public AwsService(String awsAccessKey, String awsSecretKey, String application, String environment, String config, String clientId) {
        this.awsAccessKey = awsAccessKey;
        this.awsSecretKey = awsSecretKey;
        this.application = application;
        this.environment = environment;
        this.config = config;
        this.clientId = clientId;
    }

    private AmazonAppConfig getAmazonAppConfig() {
        if(environment.equalsIgnoreCase("Development")) {
            BasicAWSCredentials awsCredentials = new BasicAWSCredentials(awsAccessKey, awsSecretKey);
            return AmazonAppConfigClient
                    .builder()
                    .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                    .withRegion(Regions.US_EAST_1)
                    .build();
        }else{
            return AmazonAppConfigClient
                    .builder()
                    .withRegion(Regions.US_EAST_1)
                    .build();
        }
    }

    public JSONObject getAppConfigData() {
        GetConfigurationRequest getConfigurationRequest = new GetConfigurationRequest()
                .withApplication(application)
                .withEnvironment(environment)
                .withConfiguration(config)
                .withClientId(clientId);

        GetConfigurationResult getConfigurationResult = getAmazonAppConfig()
                .getConfiguration(getConfigurationRequest);

        ByteBuffer appConfigContent = getConfigurationResult.getContent();
        return new JSONObject(new String(appConfigContent.array(), StandardCharsets.UTF_8));
    }

}
