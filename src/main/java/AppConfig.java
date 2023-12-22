import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jknack.handlebars.*;
import org.json.JSONObject;
import picocli.CommandLine;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

@CommandLine.Command(name = "appConfig", description = "appconfig")
public final class AppConfig implements Runnable {

    private static AwsService awsService;

    @CommandLine.Option(names = {"--application"}, description = "Application name")
    public static String application;

    @CommandLine.Option(names = {"--environment"}, description = "Environment type")
    public static String environment;

    @CommandLine.Option(names = {"--config"}, description = "Configuration name")
    public static String configuration;

    @CommandLine.Option(names = {"--clientId"}, description = "ClientId")
    public static String clientId;
    @CommandLine.Option(names = {"--inputFilePath"}, description = "InputFile path")
    public static String inputFilePath;

    @CommandLine.Option(names = {"--outputFilePath"}, description = "OutputFile path")
    public static String outputFilePath;

    @CommandLine.Option(names = {"--accessKey"}, description = "AccessKey value")
    public static String accessKey;

    @CommandLine.Option(names = {"--secretKey"}, description = "SecretKey value")
    public static String secretKey;

    public static void main(String[] args) throws Exception {
        CommandLine.run(new AppConfig(), System.err, args);
    }

    public void createApplicationPropertiesFile(String inputFilePath, String outputFilePath) throws IOException {
        String content = Files.readString(Paths.get(inputFilePath));
        try (PrintWriter printWriter = new PrintWriter(outputFilePath)) {
            printWriter.write(updateFileWithData(content, awsService.getAppConfigData()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String updateFileWithData(String propertiesTemplate, JSONObject values) throws IOException {
        JsonNode propertiesData = new ObjectMapper().readValue(values.toString(), JsonNode.class);
        Handlebars handlebars = new Handlebars();
        handlebars.registerHelper("json", Jackson2Helper.INSTANCE);

        Context context = Context
                .newBuilder(propertiesData)
                .resolver(JsonNodeValueResolver.INSTANCE)
                .build();
        String templateString = null;
        try {
            Template template = handlebars.compileInline(propertiesTemplate);
            templateString = template.apply(context);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return templateString;
    }

    @Override
    public void run() {
        awsService = new AwsService(accessKey, secretKey, application, environment, configuration, clientId);
        try {
            new AppConfig().createApplicationPropertiesFile(inputFilePath, outputFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}