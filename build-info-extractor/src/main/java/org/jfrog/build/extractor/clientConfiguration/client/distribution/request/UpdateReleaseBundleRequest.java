package org.jfrog.build.extractor.clientConfiguration.client.distribution.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Represents a request to update/create a release bundle for Distribution.
 */
public class UpdateReleaseBundleRequest extends SignReleaseBundleRequest{

    @JsonProperty("dry_run")
    boolean dryRun;
    @JsonProperty("sign_immediately")
    boolean signImmediately;
    String description;
    @JsonProperty("release_notes")
    ReleaseNotes releaseNotes;

    public boolean isDryRun() {
        return dryRun;
    }

    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    public boolean isSignImmediately() {
        return signImmediately;
    }

    public void setSignImmediately(boolean signImmediately) {
        this.signImmediately = signImmediately;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ReleaseNotes getReleaseNotes() {
        return releaseNotes;
    }

    public void setReleaseNotes(ReleaseNotes releaseNotes) {
        this.releaseNotes = releaseNotes;
    }


    public class ReleaseNotes {
        String syntax;
        String content;

        public String getSyntax() {
            return syntax;
        }

        public void setSyntax(String syntax) {
            this.syntax = syntax;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    public class Spec {
        List<Query> queries;

        public List<Query> getQueries() {
            return queries;
        }

        public void setQueries(List<Query> queries) {
            this.queries = queries;
        }
    }

    public class Query {
        String aql;
        List<Mappings> mappings;
        @JsonProperty("added_props")
        List<AddedProps> addedProps;

        public String getAql() {
            return aql;
        }

        public void setAql(String aql) {
            this.aql = aql;
        }

        public List<Mappings> getMappings() {
            return mappings;
        }

        public void setMappings(List<Mappings> mappings) {
            this.mappings = mappings;
        }

        public List<AddedProps> getAddedProps() {
            return addedProps;
        }

        public void setAddedProps(List<AddedProps> addedProps) {
            this.addedProps = addedProps;
        }
    }

    public class Mappings {
        String input;
        String output;

        public String getInput() {
            return input;
        }

        public void setInput(String input) {
            this.input = input;
        }

        public String getOutput() {
            return output;
        }

        public void setOutput(String output) {
            this.output = output;
        }
    }

    public class AddedProps {
        String key;
        List<String> value;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public List<String> getValue() {
            return value;
        }

        public void setValue(List<String> value) {
            this.value = value;
        }
    }
}
