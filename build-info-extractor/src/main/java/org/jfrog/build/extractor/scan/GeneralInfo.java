package org.jfrog.build.extractor.scan;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;

/**
 * @author yahavi
 */
@SuppressWarnings("unused")
public class GeneralInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String componentId = "";
    private String pkgType = "";
    private String path = "";
    private String sha1 = "";

    @SuppressWarnings("WeakerAccess")
    public GeneralInfo() {
    }

    public GeneralInfo(String componentId, String path, String pkgType) {
        this.componentId = componentId;
        this.path = path;
        this.pkgType = pkgType;
    }

    public String getGroupId() {
        return isValid() ? componentId.substring(0, componentId.indexOf(":")) : "";
    }

    public String getArtifactId() {
        if (!isValid()) {
            return "";
        }
        int indexOfColon = componentId.indexOf(":");
        if (StringUtils.countMatches(componentId, ":") == 1) {
            return componentId.substring(0, indexOfColon);
        }
        return componentId.substring(indexOfColon + 1, componentId.lastIndexOf(":"));
    }

    public String getVersion() {
        return isValid() ? componentId.substring(componentId.lastIndexOf(":") + 1) : "";
    }

    public String getComponentId() {
        return componentId;
    }

    public String getPath() {
        return path;
    }

    public String getPkgType() {
        return pkgType;
    }

    public String getSha1() {
        return sha1;
    }

    public GeneralInfo componentId(String componentId) {
        this.componentId = componentId;
        return this;
    }

    public GeneralInfo path(String path) {
        this.path = path;
        return this;
    }

    public GeneralInfo pkgType(String pkgType) {
        this.pkgType = pkgType;
        return this;
    }

    public GeneralInfo sha1(String sha1) {
        this.sha1 = sha1;
        return this;
    }

    private boolean isValid() {
        int colonCount = StringUtils.countMatches(componentId, ":");
        return colonCount == 1 || colonCount == 2;
    }
}