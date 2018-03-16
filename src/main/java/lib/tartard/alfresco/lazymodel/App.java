package lib.tartard.alfresco.lazymodel;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.alfresco.repo.dictionary.M2Model;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Locale;
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

    public static void main(String[] args) throws FileNotFoundException {


        String modelXmlFilePath = args[0];
        String[] fileOptions = {"i18n", "java"};
        LOGGER.info(modelXmlFilePath);

        String userDir = System.getProperty("user.dir");
        LOGGER.debug("User dir : " + userDir);

        InputStream modelStream = new FileInputStream(new File(modelXmlFilePath));
        if (modelStream == null)
        {
            throw new RuntimeException("Could not find bootstrap model " + modelXmlFilePath);
        }

        M2Model model = M2Model.createModel(modelStream);

        Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
        cfg.setWhitespaceStripping(true);

        // Where do we load the templates from:
        cfg.setClassLoaderForTemplateLoading(App.class.getClassLoader(), "templates");

        // Some other recommended settings:
        cfg.setDefaultEncoding("UTF-8");
        cfg.setLocale(Locale.US);
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);


        Map<String, Object> ftlModel = new HashMap<String, Object>();

        ftlModel.put("model", model);

        for(String fileOption : fileOptions) {
            FileOption option = FileOption.valueOf(fileOption.toUpperCase());//new FileOption(fileOption);FileOption.valueOf(fileOption);//FileOption.valueOf(fileOption);

            try {
                Template template = cfg.getTemplate(option.getTemplateName(), option.getOutputEncoding());
                Writer fileWriter = new FileWriter(new File(userDir, option.resolveOutputFileName(modelXmlFilePath, model)));
                try {
                    template.process(ftlModel, fileWriter);
                } catch (TemplateException e) {
                    e.printStackTrace();
                } finally {
                    fileWriter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }



    }


    public enum FileOption {

        I18N("i18n") {
            String resolveOutputFileName(String modelFilePath, M2Model model) {
                return FilenameUtils.getBaseName(modelFilePath) + ".properties";
            }

            String getTemplateName() {
                return "i18n-properties.ftl";
            }

            String getOutputEncoding() {
                return "ISO-8859-1";
            }
        },

        JAVA("java") {
            String resolveOutputFileName(String modelFilePath, M2Model model) {
                return "model.java";
            }

            String getTemplateName() {
                return "javamodel.ftl";
            }

            String getOutputEncoding() {
                return "UTF-8";
            }
        };


        FileOption(String opt) {
        }

        abstract String resolveOutputFileName(String modelFilePath, M2Model model);
        abstract String getTemplateName();
        abstract String getOutputEncoding();


    }

}

