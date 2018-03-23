package lib.tartard.alfresco.lazymodel.test.services;

import freemarker.template.TemplateException;
import lib.tartard.alfresco.lazymodel.services.ConversionConfig;
import lib.tartard.alfresco.lazymodel.services.ModelConversionException;
import lib.tartard.alfresco.lazymodel.services.ModelConverter;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * ModelConverter test class.
 *
 * @author Alexandre Hausherr b88779
 * <p>
 * Created by b88779 on 20/03/2018
 */
@RunWith(JUnit4.class)
public class ModelConverterTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModelConverterTest.class);

    private ModelConverter modelConverter = new ModelConverter();

    private static final String MODELS_FOLDER_PATH = "models/";
    private static final String TEMPLATES_FOLDER_PATH = "templates/";
    private static final String CONTENT_MODEL_NAME = "contentModel.xml";
    private static final String FAKE_MODEL_NAME = "fakeModel.xml";
    private static final String FAKE_TEMPLATE_NAME = "fakeTemplate.ftl";
    private static final String OUTPUT_FOLDER_PATH = "target/output";


    @BeforeClass
    public static void setup() throws IOException {
        Files.createDirectories(Paths.get(OUTPUT_FOLDER_PATH));
    }


    /**
     * Tests that the freemarker templates folder given to the converter exists.
     * @throws IOException
     */
    @Test
    public void testSetFreemarkerTemplatesFolder() throws IOException {
        // Setting the templates folder with a folder which doesn't exist should raise an error.
        String folderPath = "/" + UUID.randomUUID();
        try {
            modelConverter.setFreemarkerTemplatesFolder(folderPath);
            fail("A ModelConversionException should have been raised.");
        } catch (ModelConversionException e) {
            assertTrue(e.getCause() instanceof IOException);
        }

        // Setting the tempaltes folder with an existing folder should be ok and raise no exception.
        String folder = Files.createTempDirectory(UUID.randomUUID().toString()).toString();
        modelConverter.setFreemarkerTemplatesFolder(folder);
    }

    /**
     * Model path is mandatory.
     */
    @Test
    public void testConvertWithoutModelPath() {
        ConversionConfig config = new ConversionConfig();
        testException(config, null);
    }

    /**
     * Model path should lead to an existing file.
     */
    @Test
    public void testConvertWithWrongModelPath() {
        ConversionConfig config = new ConversionConfig();
        config.setAlfrescoModelPath("/" + UUID.randomUUID() + ".xml");
        testException(config, FileNotFoundException.class);
    }

    /**
     * The model file should be a valid Alfresco xml model.
     */
    @Test
    public void testConvertBadModel() {
        ConversionConfig config = new ConversionConfig();
        config.setAlfrescoModelPath(getModel(FAKE_MODEL_NAME).getAbsolutePath());
        testException(config, DictionaryException.class);
    }

    /**
     * Template path is mandatory.
     */
    @Test
    public void testNoTemplatePathProvided() {
        ConversionConfig config = new ConversionConfig();
        config.setAlfrescoModelPath(getModel(CONTENT_MODEL_NAME).getAbsolutePath());
        testException(config, null);
    }

    /**
     * Template folder should be a real folder.
     */
    @Test
    public void testCannotSetTemplateFolder() {
        ConversionConfig config = new ConversionConfig();
        config.setAlfrescoModelPath(getModel(CONTENT_MODEL_NAME).getAbsolutePath());
        // Set a non existing location for template
        config.setFtlTemplatePath(Paths.get(UUID.randomUUID().toString(), UUID.randomUUID().toString()).toString());
        testException(config, IOException.class);
    }

    /**
     * The given template path should lead to an existing file.
     */
    @Test
    public void testTemplateDoesntExist() {
        ConversionConfig config = new ConversionConfig();
        config.setAlfrescoModelPath(getModel(CONTENT_MODEL_NAME).getAbsolutePath());
        // Set a non existing location for template
        config.setFtlTemplatePath(Paths.get(UUID.randomUUID().toString() + ".xml").toString());
        testException(config, IOException.class);
    }

    /**
     * Output file path is mandatory.
     */
    @Test
    public void testNoOutputFileProvided() {
        ConversionConfig config = new ConversionConfig();
        config.setAlfrescoModelPath(getModel(CONTENT_MODEL_NAME).getAbsolutePath());
        config.setFtlTemplatePath(getModel(CONTENT_MODEL_NAME).getAbsolutePath());
        testException(config, null);
    }

    /**
     * The given template should be a valid ftl template with only one variable : ${model}.
     * @throws IOException
     */
    @Test
    public void testTemplateException() throws IOException {
        ConversionConfig config = new ConversionConfig();
        config.setAlfrescoModelPath(getModel(CONTENT_MODEL_NAME).getAbsolutePath());
        // Set a non existing location for template
        config.setFtlTemplatePath(getTemplate(FAKE_TEMPLATE_NAME).getAbsolutePath());
        config.setOutputFile(OUTPUT_FOLDER_PATH + "/" + UUID.randomUUID().toString());
        testException(config, TemplateException.class);
    }

    /**
     * Cannot write to output file.
     */
    @Test
    public void testCannotWriteToFile() {
        ConversionConfig config = new ConversionConfig();
        config.setAlfrescoModelPath(getModel(CONTENT_MODEL_NAME).getAbsolutePath());
        // Set a non existing location for template
        config.setFtlTemplatePath(getTemplate("java.UTF-8.ftl").getAbsolutePath());
        config.setOutputFile(".");
        testException(config, IOException.class);
    }

    /**
     * Conversion should work if valid parameters are given.
     * @throws IOException
     */
    @Test
    public void testConversionOk() throws IOException {
        ConversionConfig config = new ConversionConfig();
        config.setAlfrescoModelPath(getModel(CONTENT_MODEL_NAME).getAbsolutePath());
        // Set a non existing location for template
        config.setFtlTemplatePath(getTemplate("java.UTF-8.ftl").getAbsolutePath());
        File outputFile = new File(OUTPUT_FOLDER_PATH, "lazymodeltest.txt");
        config.setOutputFile(outputFile.getPath());
        config.setOutputEncoding("UTF-8");
        File result = modelConverter.convert(config);
        assertTrue(result.isFile());
        assertEquals(outputFile, result);
    }

    /**
     * Tests that the conversion of the given {@code config} will throw a ModelConversionException caused by the given
     * {@code expectedParentException}, or caused by nothing if {@code expectedParentException} is null.
     * @param config
     * @param expectedParentException
     */
    private void testException(ConversionConfig config, Class<? extends Exception> expectedParentException) {
        try {
            modelConverter.convert(config);
            fail("A(n) " + expectedParentException.getSimpleName() + " exception should have been raised.");
        } catch (ModelConversionException e) {
            Throwable cause = e.getCause();
            if(expectedParentException == null) {
                assertNull("This exception should be the root cause.", cause);
                return;
            }
            else if(cause == null) {
                fail("No root cause but expected " + expectedParentException.getSimpleName());
            }
            LOGGER.debug("Parent Exception class : {}", cause.getClass());
            LOGGER.debug("Parent Exception message : {}", cause.getMessage());
            assertTrue("Exception should come from " + expectedParentException + " but comes from " + cause.getClass(),
                    expectedParentException.isAssignableFrom(cause.getClass()));
        }
    }

    /**
     * Get an Alfresco model from the folder resources/models by its fileName.
     * @param name
     * @return
     */
    private File getModel(String name) {
        return getResource(MODELS_FOLDER_PATH + name);
    }

    /**
     * Get a ftl template from the folder resources/templates by its fileName.
     * @param name
     * @return
     */
    private File getTemplate(String name) {
        return getResource(TEMPLATES_FOLDER_PATH + name);
    }

    private File getResource(String path) {
        return new File(this.getClass().getClassLoader().getResource(path).getFile());
    }
}
