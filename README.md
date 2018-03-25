# Alfresco lazymodel


Command line tool to create Alfresco model-related files.

Currently, the program can generate the model properties file (for i18n) and the Java class which contains the QNames of
the given model.

## Usage


Download **lazymodel-pckg.zip** and unzip it.

You get the following structure :
* lazymodel.jar
* templates
    * properties.ISO-8859-1.ftl
    * java.UTF-8.ftl

To generate files, run :

`$ java -jar lazymodel.jar path/to/the/xml/alfresco/model/file.xml`

You'll then find two files at your current location : <model-name>.java and <model-name>.properties.

It is possible to specify where you want the program to take the ftl templates from with the **-t** option, and where to 
generate files with the **-o** option.

Program usage :

    usage: java -jar lazymodel.jar <Model Path> [Options]
     -help         Print this message.
     -o <folder>   Output folder path. Default is user's current folder.
     -t <folder>   Freemarker templates folder path.



## Use your custom templates

The program uses Freemarker to generate files from an Alfresco model with a dedicated template.

If you have another kind of file to generate from a model, like a share form config for example, you can create your 
dedicated ftl template.

For the program to be able to read and process your template, it has to respect the following conditions :
 * The name of the template must match the pattern **{output file extension}.{output file encoding}.ftl**.
 * The only freemarker variable you can use in the template is **${model}**, the **M2Model** object.
 
 
M2Model.java source :https://github.com/Alfresco/alfresco-data-model/blob/master/src/main/java/org/alfresco/repo/dictionary/M2Model.java

M2Model.java javadoc :
http://docs.huihoo.com/javadoc/alfresco/4.2/datamodel/org/alfresco/repo/dictionary/M2Model.html

If you create a template which you think can be usefull to many people, feel free to submit a pull request for me to add 
it to the project.