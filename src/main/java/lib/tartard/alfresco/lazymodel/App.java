package lib.tartard.alfresco.lazymodel;

import lib.tartard.alfresco.lazymodel.services.ConversionConfig;
import lib.tartard.alfresco.lazymodel.services.ModelConversionException;
import lib.tartard.alfresco.lazymodel.services.ModelConverter;
import org.apache.commons.cli.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 *
 * @author Alexandre Hausherr
 *
 * Created on 13/10/2017
 */
public class App {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    private static final String COMMAND = "java -jar lazymodel.jar <Model Path> [Options]";

    private static final String DEFAULT_TEMPLATE_FOLDER = "/templates";

    private static Options getOptions(String defaultTemplatesPath) {
        Options options = new Options();

        Option help = new Option("help", "Print this message.");

        Option templates = Option.builder("t")
                .argName("folder")
                .hasArg()
                .desc("Freemarker templates folder path. Default is '" + defaultTemplatesPath + "'.")
                .build();

        Option outputFolder = Option.builder("o")
                .argName("folder")
                .hasArg()
                .desc("Output folder path. Default is user's current folder.")
                .build();

        options.addOption(help);
        options.addOption(outputFolder);
        options.addOption(templates);

        return options;
    }


    public static void main(String[] args) throws ParseException, URISyntaxException {
        final File jarFolder = new File(App.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        String defaultTemplatesFolder =
                jarFolder.isDirectory() ?
                        jarFolder.getAbsolutePath() + DEFAULT_TEMPLATE_FOLDER :
                        jarFolder.getParent() + DEFAULT_TEMPLATE_FOLDER;


        CommandLineParser parser = new DefaultParser();
        Options options = getOptions(defaultTemplatesFolder);
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = parser.parse(options, args);

        String[] pgArgs = cmd.getArgs();
        String modelXmlFilePath = null;
        if(cmd.hasOption("help") || (pgArgs == null || pgArgs.length < 1 || (StringUtils.isBlank(modelXmlFilePath = pgArgs[0])))) {
            if(!cmd.hasOption("help")) {
                LOGGER.error("Missing Alfresco model file path.");
            }
            formatter.printHelp(COMMAND, options);
            return;
        }

        String templatesFolderPath = cmd.hasOption("t") ?
                cmd.getOptionValue("t") :
                defaultTemplatesFolder;

        String outputFolder = cmd.hasOption("o") ?
                cmd.getOptionValue("o") :
                System.getProperty("user.dir");



        ModelConverter modelConverter = new ModelConverter(templatesFolderPath);

        File templatesFolder = new File(templatesFolderPath);
        List<ConversionConfig> configs = new ArrayList<>();
        Map<String, Integer> names = new HashMap<>();
        for(File child : templatesFolder.listFiles()) {
            String[] ftlParts = child.getName().split("\\.");
            String encoding = ftlParts.length >= 3 ? ftlParts[1] : "UTF-8";
            String extension = ftlParts.length >= 2 ? ftlParts[0] : null;
            String outputFileName = FilenameUtils.getBaseName(modelXmlFilePath) + FilenameUtils.EXTENSION_SEPARATOR
                    + extension;
            Integer index = names.get(outputFileName);
            if(index != null) {
                outputFileName += "(" + ++index + ")";
            }

            ConversionConfig config = new ConversionConfig(
                    modelXmlFilePath,
                    child.getName(),
                    outputFolder + "/" + outputFileName,
                    encoding
            );

            configs.add(config);
        }
        try {
            modelConverter.convert(configs);
        } catch (ModelConversionException e) {
            // Do nothing, exception is already logged by the service
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

