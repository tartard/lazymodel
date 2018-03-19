package lib.tartard.alfresco.lazymodel.services;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Configuration bean for the ModelConverter.
 * This object contains the information required to generate a file from an Alfresco model, i.e :
 * - The path of the Alfresco model file
 * - The path of the freemarker template
 * - The path of the file to be created from the Alfresco model
 * - The encoding of the output file
 *
 * @author Alexandre Hausherr
 * @version 1.0 18/03/2018
 */
public class ConversionConfig {

    private String ftlTemplatePath;

    private String outputEncoding;

    private String alfrescoModelPath;

    private String outputFile;

    public ConversionConfig() {}

    public ConversionConfig(String alfrescoModelPath, String ftlTemplatePath, String outputFile, String outputEncoding) {
        this.alfrescoModelPath = alfrescoModelPath;
        this.ftlTemplatePath = ftlTemplatePath;
        this.outputFile = outputFile;
        this.outputEncoding = outputEncoding;
    }

    public String getFtlTemplatePath() {
        return ftlTemplatePath;
    }

    public void setFtlTemplatePath(String ftlTemplatePath) {
        this.ftlTemplatePath = ftlTemplatePath;
    }

    public String getOutputEncoding() {
        return outputEncoding;
    }

    public void setOutputEncoding(String outputEncoding) {
        this.outputEncoding = outputEncoding;
    }

    public String getAlfrescoModelPath() {
        return alfrescoModelPath;
    }

    public void setAlfrescoModelPath(String alfrescoModelPath) {
        this.alfrescoModelPath = alfrescoModelPath;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    @Override
    public boolean equals(Object other) {
        return EqualsBuilder.reflectionEquals(this, other);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
