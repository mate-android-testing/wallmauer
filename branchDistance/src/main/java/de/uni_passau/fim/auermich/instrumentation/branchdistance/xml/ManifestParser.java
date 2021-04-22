package de.uni_passau.fim.auermich.instrumentation.branchdistance.xml;

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
import java.util.Objects;

public class ManifestParser {

    private static final Logger LOGGER = LogManager.getLogger(ManifestParser.class);

    private final String MANIFEST;

    private String packageName;
    private String mainActivity;

    public ManifestParser(String manifest) {
        MANIFEST = manifest;
    }

    /**
     * Parses the AndroidManifest.xml for the package name and the name of the main activity.
     *
     * @return Returns {@code true} when we were able to derive both information,
     * otherwise {@code false}.
     */
    public boolean parseManifest() {

        LOGGER.info("Parsing AndroidManifest for MainActivity and PackageName!");

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

                if (!element.hasAttribute("package")) {
                    LOGGER.info("Couldn't derive package name!");
                    return false;
                } else {
                    // we need to add a missing slash to the packageName
                    packageName = element.getAttribute("package") + "/";
                }
            }

            NodeList intentFilters = doc.getElementsByTagName("intent-filter");
            final String NAME_ATTRIBUTE = "android:name";
            final String ALIAS_NAME_ATTRIBUTE = "android:targetActivity";

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
            LOGGER.warn("Couldn't derive name of main-activity");
        } catch (Exception e) {
            LOGGER.warn("Couldn't parse AndroidManifest.xml");
            LOGGER.warn(e.getMessage());
        }
        return false;
    }

    /**
     * Marks the application as debuggable.
     *
     * @return Returns {@code true} if inserting the debuggable attribute succeeded,
     *      otherwise {@code false}.
     */
    public boolean addDebuggableFlag() {

        LOGGER.info("Adding debuggable attribute to Manifest!");

        try {

            File xmlFile = new File(MANIFEST);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);

            NodeList nodeList = doc.getElementsByTagName("application");

            // there is just a single application tag
            assert nodeList.getLength() == 1;
            Node applicationTag = nodeList.item(0);

            if (applicationTag.getNodeType() == Node.ELEMENT_NODE) {

                Element application = (Element) applicationTag;
                final String debuggableAttribute = "android:debuggable";

                // check whether the application tag already defines a debuggable attribute
                if (application.hasAttribute(debuggableAttribute)) {
                    boolean isDebuggable = Boolean.parseBoolean(application.getAttribute(debuggableAttribute));
                    if (isDebuggable) {
                        // the app is already debuggable
                        return true;
                    } else {
                        // change the attribute value to true
                        application.setAttribute(debuggableAttribute, "true");
                    }
                } else {
                    // we need to add the attribute android:debuggable
                    application.setAttribute(debuggableAttribute, "true");
                }
            } else {
                // should never happen
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
            LOGGER.warn("Couldn't parse AndroidManifest.xml");
            LOGGER.warn(e.getMessage());
        }
        return false;
    }

    public boolean addPermissionTag(String permission) {

        LOGGER.info("Adding permission " + permission + " to Manifest!");

        try {

            File xmlFile = new File(MANIFEST);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);

            NodeList nodeList = doc.getElementsByTagName("uses-permission");

            if (nodeList.getLength() == 0) {
                // there are no permissions specified

                Element permissionTag = doc.createElement("uses-permission");
                permissionTag.setAttribute("android:name", permission);
                // add as child of root tag <xml>
                doc.getDocumentElement().appendChild(permissionTag);
            } else {

                // check whether the given permission is already specified
                for (int i=0; i < nodeList.getLength(); i++) {
                    Element permissionTag = (Element) nodeList.item(i);
                    if (permissionTag.getAttribute("android:name").equals(permission)) {
                        return true;
                    }
                }

                Element permissionTag = doc.createElement("uses-permission");
                permissionTag.setAttribute("android:name", permission);
                // add as child of root tag <xml>
                doc.getDocumentElement().appendChild(permissionTag);
            }

            // modify manifest
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(xmlFile);
            transformer.transform(source, result);

            return true;
        } catch (Exception e) {
            LOGGER.warn("Couldn't parse AndroidManifest.xml");
            LOGGER.warn(e.getMessage());
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

        LOGGER.info("Adding BroadcastReceiver to AndroidManifest!");

        try {
            File xmlFile = new File(MANIFEST);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);

            NodeList nodeList = doc.getElementsByTagName("application");

            // there is just a single application tag
            assert nodeList.getLength() == 1;
            Node applicationTag = nodeList.item(0);

            Element receiver = doc.createElement("receiver");
            receiver.setAttribute("android:name", broadcastReceiver);
            receiver.setAttribute("android:exported", "true");

            // add intent filter
            Element intentFilter = doc.createElement("intent-filter");

            // add action tag
            Element action = doc.createElement("action");
            action.setAttribute("android:name", actionName);

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
            LOGGER.warn("Couldn't parse AndroidManifest.xml");
            LOGGER.warn(e.getMessage());
        }
        return false;
    }

    public String getPackageName() {
        Objects.requireNonNull(packageName);
        return packageName;
    }

    public String getMainActivity() {
        Objects.requireNonNull(mainActivity);
        return mainActivity;
    }
}
