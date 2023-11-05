package de.uni_passau.fim.auermich.instrumentation.basicblockcoverage.xml;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

public class ManifestParser {

    private static final Logger LOGGER = LogManager.getLogger(ManifestParser.class);

    private final String MANIFEST;

    private String packageName;
    private String mainActivity;

    /**
     * Specifies the name space prefix. According to the docs, see https://developer.android.com/guide/topics/manifest/manifest-element,
     * it should be always 'android', but certain apps deviate from this rule and use an arbitrary name space prefix.
     */
    private String nameSpacePrefix = null;

    public ManifestParser(String manifest) {
        MANIFEST = manifest;
    }

    /**
     * Parses the AndroidManifest.xml for the package name and the name of the main activity.
     *
     * @return Returns {@code true} when we were able to derive both information,
     *         otherwise {@code false}.
     */
    public boolean parseManifest() {

        LOGGER.debug("Parsing AndroidManifest for MainActivity and PackageName!");

        try {
            File xmlFile = new File(MANIFEST);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);

            NodeList nodeList = doc.getElementsByTagName("manifest");
            // there should be only a single manifest tag
            Node node = nodeList.item(0);

            // get the package name
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;

                // extract a possible custom name space prefix - default one is 'android', see:
                // https://developer.android.com/guide/topics/manifest/manifest-element
                for (int i=0; i < element.getAttributes().getLength(); i++) {
                    final Node manifestAttribute = element.getAttributes().item(i);
                    if (manifestAttribute.getNodeName().startsWith("xmlns:")
                            && manifestAttribute.getNodeValue().equals("http://schemas.android.com/apk/res/android")) {
                        nameSpacePrefix = manifestAttribute.getNodeName().split("xmlns:")[1];
                        if (!nameSpacePrefix.equals("android")) {
                            LOGGER.debug("Custom name space prefix: " + nameSpacePrefix);
                        }
                        break;
                    }
                }

                if (!element.hasAttribute("package")) {
                    LOGGER.error("Couldn't derive package name!");
                    return false;
                } else {
                    packageName = element.getAttribute("package");
                }
            }

            NodeList intentFilters = doc.getElementsByTagName("intent-filter");
            final String NAME_ATTRIBUTE = nameSpacePrefix == null ? "android:name" : nameSpacePrefix + ":name";
            final String ALIAS_NAME_ATTRIBUTE = nameSpacePrefix == null ? "android:targetActivity" : nameSpacePrefix + ":targetActivity";

            // find intent-filter that describes the main activity
            for (int i = 0; i < intentFilters.getLength(); i++) {

                Node intentFilter = intentFilters.item(i);
                NodeList tags = intentFilter.getChildNodes();

                boolean foundMainAction = false;
                boolean foundMainCategory = false;

                for (int j = 0; j < tags.getLength(); j++) {
                    Node tag = tags.item(j);
                    if (tag.getNodeType() == Node.ELEMENT_NODE) {
                        Element element = (Element) tag;

                        if (element.getTagName().equals("action")
                                && element.getAttribute(NAME_ATTRIBUTE)
                                .equals("android.intent.action.MAIN")) {
                            foundMainAction = true;
                        } else if (element.getTagName().equals("category")
                                && element.getAttribute(NAME_ATTRIBUTE)
                                .equals("android.intent.category.LAUNCHER")) {
                            foundMainCategory = true;
                        }

                        if (foundMainAction && foundMainCategory) {
                            Node mainActivityNode = intentFilter.getParentNode();
                            if (mainActivityNode.getNodeType() == Node.ELEMENT_NODE) {
                                Element main = (Element) mainActivityNode;
                                if (main.getTagName().equals("activity")) {
                                    mainActivity = main.getAttribute(NAME_ATTRIBUTE);
                                    return true;
                                } else if (main.getTagName().equals("activity-alias")) {
                                    mainActivity = main.getAttribute(ALIAS_NAME_ATTRIBUTE);
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
            LOGGER.warn("Couldn't derive name of main activity!");
            return true;
        } catch (Exception e) {
            LOGGER.error("Couldn't parse AndroidManifest.xml: " + e.getMessage());
        }
        return false;
    }

    /**
     * Adds the given application attribute to the manifest.
     *
     * @param attributeName The attribute that should be inserted.
     * @param value The value of the attribute.
     * @return Returns {@code true} if inserting the attribute succeeded, otherwise {@code false} is returned.
     */
    public boolean addApplicationAttribute(final String attributeName, boolean value) {

        LOGGER.debug("Adding application attribute " + attributeName + " to Manifest!");

        try {

            File xmlFile = new File(MANIFEST);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);

            NodeList nodeList = doc.getElementsByTagName("application");
            final String NAME_SPACE_PREFIX = nameSpacePrefix == null ? "android" : nameSpacePrefix;
            final String attribute = NAME_SPACE_PREFIX + ":" + attributeName;

            // there is just a single application tag
            assert nodeList.getLength() == 1;
            Node applicationTag = nodeList.item(0);

            if (applicationTag.getNodeType() == Node.ELEMENT_NODE) {

                Element application = (Element) applicationTag;

                // check whether the application tag already defines such attribute
                if (application.hasAttribute(attribute)) {
                    boolean isEnabled = Boolean.parseBoolean(application.getAttribute(attribute));
                    if (isEnabled == value) {
                        // the app has already the attribute with the correct value
                        return true;
                    } else {
                        // change the attribute value to the desired value
                        application.setAttribute(attribute, String.valueOf(value));
                    }
                } else {
                    // we need to add the attribute first
                    application.setAttribute(attribute, String.valueOf(value));
                }
            } else {
                // should never happen
                LOGGER.error("Couldn't locate application node!");
                return false;
            }

            // modify manifest
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(xmlFile);
            transformer.transform(source, result);

            return true;
        } catch (Exception e) {
            LOGGER.error("Couldn't parse AndroidManifest.xml: " + e.getMessage());
        }
        return false;
    }

    /**
     * Adds the given permission tag to the AndroidManifest.xml file.
     *
     * @param permission The permission that should be added.
     * @return Returns {@code true} if inserting the permission tag succeeded, otherwise {@code false} is returned.
     */
    public boolean addPermissionTag(String permission) {

        LOGGER.debug("Adding permission " + permission + " to Manifest!");

        try {

            File xmlFile = new File(MANIFEST);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);

            NodeList nodeList = doc.getElementsByTagName("uses-permission");
            final String NAME_ATTRIBUTE = nameSpacePrefix == null ? "android:name" : nameSpacePrefix + ":name";
            final String MAX_SDK_ATTRIBUTE = nameSpacePrefix == null ? "android:maxSdkVersion" : nameSpacePrefix + ":maxSdkVersion";

            if (nodeList.getLength() == 0) {
                // there are no permissions specified

                Element permissionTag = doc.createElement("uses-permission");
                permissionTag.setAttribute(NAME_ATTRIBUTE, permission);
                // add as child of root tag <xml>
                doc.getDocumentElement().appendChild(permissionTag);
            } else {

                boolean foundPermission = false;

                // check whether the given permission is already specified
                for (int i=0; i < nodeList.getLength(); i++) {
                    Element permissionTag = (Element) nodeList.item(i);
                    if (permissionTag.getAttribute(NAME_ATTRIBUTE).equals(permission)) {

                        /*
                        * It is possible to restrict a permission to a maximal sdk version by specifying the attribute
                        * android:maxSdkVersion. This is used in particular for the WRITE_EXTERNAL_STORAGE permission
                        * which is not required on API > 18 as long as you write to the provided storage defined by
                        * getExternalFilesDir(). We simply drop this restriction, otherwise we might not be able to
                        * read or write from/to the external storage.
                         */
                        if (permissionTag.hasAttribute(MAX_SDK_ATTRIBUTE)) {
                            permissionTag.removeAttribute(MAX_SDK_ATTRIBUTE);
                            foundPermission = true;
                            break;
                        } else {
                            return true;
                        }
                    }
                }

                if (!foundPermission) {
                    Element permissionTag = doc.createElement("uses-permission");
                    permissionTag.setAttribute(NAME_ATTRIBUTE, permission);
                    // add as child of root tag <xml>
                    doc.getDocumentElement().appendChild(permissionTag);
                }
            }

            // modify manifest
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(xmlFile);
            transformer.transform(source, result);

            return true;
        } catch (Exception e) {
            LOGGER.error("Couldn't parse AndroidManifest.xml: " + e.getMessage());
        }
        return false;
    }

    /**
     * Adds a broadcast receiver tag to the AndroidManifest file.
     *
     * @param broadcastReceiver The name (android:name) of the broadcast receiver.
     * @param actionName        The action to which the broadcast receiver reacts.
     * @return Returns {@code true} if the tag could be added, otherwise {@code false}.
     */
    public boolean addBroadcastReceiverTag(String broadcastReceiver, String actionName) {

        LOGGER.debug("Adding BroadcastReceiver to AndroidManifest!");

        try {
            File xmlFile = new File(MANIFEST);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);

            NodeList nodeList = doc.getElementsByTagName("application");
            final String NAME_ATTRIBUTE = nameSpacePrefix == null ? "android:name" : nameSpacePrefix + ":name";
            final String EXPORTED_ATTRIBUTE = nameSpacePrefix == null ? "android:exported" : nameSpacePrefix + ":exported";

            // there is just a single application tag
            assert nodeList.getLength() == 1;
            Node applicationTag = nodeList.item(0);

            Element receiver = doc.createElement("receiver");
            receiver.setAttribute(NAME_ATTRIBUTE, broadcastReceiver);
            receiver.setAttribute(EXPORTED_ATTRIBUTE, "true");

            // add intent filter
            Element intentFilter = doc.createElement("intent-filter");

            // add action tag
            Element action = doc.createElement("action");
            action.setAttribute(NAME_ATTRIBUTE, actionName);

            intentFilter.appendChild(action);
            receiver.appendChild(intentFilter);

            // add as new component
            applicationTag.appendChild(receiver);

            // modify manifest
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(xmlFile);
            transformer.transform(source, result);

            return true;
        } catch (Exception e) {
            LOGGER.error("Couldn't parse AndroidManifest.xml: " + e.getMessage());
        }
        return false;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getMainActivity() {
        return mainActivity;
    }
}
