package lib.tartard.alfresco.lazymodel.services;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

/**
 * This class allows to generate files from an Alfresco xml model.
 *
 * The file generation simply unmarshalls the given Alfreco model into a M2 model object using Alfresco's libraries.
 * Then this M2Model object is injected in the specified Freemarker template, and a new file is generated from this
 * template.
 *
 * @author Alexandre Hausherr
 * @version 1.0 18/03/2018
 */
public class ModelConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModelConverter.class);

    private Configuration freemarkerConfig;


    private static final String LOG_MESSAGE_GENERATING_FILE = "Generating file from the following configuration :\n{}";

    /**
     * Instantiate a ModelConverter with a preset Freemarker configuration.
     */
    public ModelConverter() {
        freemarkerConfig = new Configuration(Configuration.VERSION_2_3_23);
        freemarkerConfig.setWhitespaceStripping(true);
        freemarkerConfig.setDefaultEncoding("UTF-8");
        freemarkerConfig.setLocale(Locale.US);
        freemarkerConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    }


    /**
     * Instantiate a ModelConverter with a preset Freemarker configuration and a default path to Freemarker templates.
     * Once this default path set, it is possible to specify only the template name in the configuration, instead of the
     * whole template file path.
     *
     * @param freemarkerTemplatesFolderPath
     */
    public ModelConverter(String freemarkerTemplatesFolderPath) {
        this();
        setFreemarkerTemplatesFolder(freemarkerTemplatesFolderPath);
    }


    /**
     * Generates a file from the Alfresco model, with the Freemarker template, with the encoding and at the path
     * specified by the given {@code config}.
     *
     * @param config configuration to be used to generate a file.
     * @return a file generated after the given configuration and Alfresco model.
     * @see ConversionConfig
     */
    public File convert(ConversionConfig config) {
        LOGGER.info(LOG_MESSAGE_GENERATING_FILE, config);
        return convert(getM2Model(config.getAlfrescoModelPath()), config);
    }


    /**
     * Generates files from the Alfresco models, with the Freemarker templates, with the encoding and at the paths
     * specified by the given {@code configs}.
     * Performs the same thing as {@see convert} with several configurations.
     *
     * @param configs list of configurations to be used to generate a file.
     * @return a list of files geenrated after the given configurations and Alfresco models.
     * @see ConversionConfig
     */
    public List<File> convert(List<ConversionConfig> configs) {
        M2Model model = null;
        String modelPath = null;
        List<File> outputFiles = new ArrayList<>(configs.size());
        for (ConversionConfig config : configs) {
            LOGGER.info(LOG_MESSAGE_GENERATING_FILE, config);
            if (modelPath == null) {
                modelPath = config.getAlfrescoModelPath();
            }
            if (model == null || !modelPath.equals(config.getAlfrescoModelPath())) {
                model = getM2Model(modelPath);
            }
            try {
                outputFiles.add(convert(model, config));
            } catch (ModelConversionException e) {
                // As we want the other conversions to be performed, we only print the stackTrace.
                e.printStackTrace();
            }
        }
        return outputFiles;
    }


    /**
     * Set a default path to Freemarker templates.
     * Once this default path set, it is possible to specify only the template name in the configuration, instead of the
     * whole template file path.
     * @param templatesFolder
     */
    public void setFreemarkerTemplatesFolder(String templatesFolder) {
        try {
            freemarkerConfig.setDirectoryForTemplateLoading(new File(templatesFolder));
        } catch (IOException e) {
            throw getException("Could not load templates folder '" + templatesFolder + "' due to nested Exceptions :", e);
        }
    }


    /**
     * Get the Freemarker template specified by the given configuration.
     * @param config
     * @return
     */
    private Template getTemplate(ConversionConfig config) {
        String templatePath = config.getFtlTemplatePath();
        LOGGER.trace("Getting ftl template from file '{}'.", templatePath);

        if(StringUtils.isBlank(templatePath)) {
            throw getException("No template file path provided. Cannot convert model.");
        }

        File templateFile = new File(templatePath);
        File parentDir = templateFile.getParentFile();

        if(parentDir != null) {
            try {
                freemarkerConfig.setDirectoryForTemplateLoading(parentDir);
            } catch (IOException e) {
                throw getException("Could not find template at '" + templatePath + "' due to nested Exception :", e);
            }
        }

        try {
            return freemarkerConfig.getTemplate(templateFile.getName(), config.getOutputEncoding());
        } catch (IOException e) {
            throw getException("No template found at the specified path '" + templatePath + "' :", e);
        }
    }


    /**
     * Generates a file from the given M2Model and configuration.
     * @param m2Model
     * @param config
     * @return
     */
    private File convert(M2Model m2Model, ConversionConfig config) {
        Template template = getTemplate(config);
        Map<String, Object> ftlModel = getFtlModel(m2Model, config);
        String outputFilePath = config.getOutputFile();
        if(StringUtils.isBlank(outputFilePath)) {
            throw getException("No output file path provided. Cannot perform model conversion.");
        }
        File outPutFile = new File(outputFilePath);
        try (Writer fileWriter = new FileWriter(outPutFile)) {
            template.process(ftlModel, fileWriter);
        } catch (TemplateException e) {
            throw getException("Could not process template due to nested Exception :", e);
        } catch (IOException e) {
            throw getException("Could not write to output file due to nested Exception :", e);
        }
        return outPutFile;
    }


    /**
     * Build the model for the ftl template with the given M2 model and config.
     * @param m2Model
     * @param config
     * @return
     */
    private Map<String, Object> getFtlModel(M2Model m2Model, ConversionConfig config) {
        Map<String, Object> ftlModel = new HashMap<>();
        ftlModel.put("model", m2Model);
        return ftlModel;
    }


    /**
     * Get the M2 model object from the xml model file located at the given path.
     * @param modelPath path of the xml model file
     * @return the M2 model built from the given file
     */
    private M2Model getM2Model(String modelPath) {
        LOGGER.trace("Getting M2Model from file '{}'.", modelPath);
        InputStream modelStream = null;
        if(StringUtils.isBlank(modelPath)) {
            throw getException("No model path provided. Cannot convert model.");
        }
        try {
            modelStream = new FileInputStream(new File(modelPath));
        } catch (FileNotFoundException e) {
            throw getException("There is no xml model at the given location '" + modelPath + "'.", e);
        }
        try {
            return M2Model.createModel(modelStream);
        } catch (DictionaryException e) {
            throw getException("Failed to create M2Model due to nested Exception :", e);
        }
    }


    /**
     * Create an exception with the given message and log the message as an error.
     * @param msg
     * @return
     */
    private ModelConversionException getException(String msg) {
        LOGGER.error(msg);
        return new ModelConversionException(msg);
    }


    /**
     * Create an exception with the given message and the given cause, and log the message as an error.
     * @param msg
     * @param cause
     * @return
     */
    private ModelConversionException getException(String msg, Exception cause) {
        LOGGER.error("{} {}", msg, cause.getMessage());
        return new ModelConversionException(msg, cause);
    }
}
