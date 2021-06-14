package org.jfrog.build.extractor.clientConfiguration.client.distribution.request;

import org.jfrog.build.extractor.clientConfiguration.client.distribution.types.ReleaseBundleQuery;
import org.jfrog.build.extractor.clientConfiguration.client.distribution.types.ReleaseBundleSpec;
import org.jfrog.build.extractor.clientConfiguration.util.spec.Spec;
import org.jfrog.filespecs.DistributionHelper;
import org.jfrog.filespecs.distribution.DistributionSpecComponent;
import org.jfrog.filespecs.entities.Aql;
import org.jfrog.filespecs.entities.FilesGroup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yahavi
 **/
public class Utils {

    public static ReleaseBundleSpec createSpec(Spec fileSpec) throws IOException {
        List<ReleaseBundleQuery> queries = new ArrayList<>();
        List<DistributionSpecComponent> specComponents = DistributionHelper.toSpecComponents(toFileSpec(fileSpec));
        for (DistributionSpecComponent specComponent : specComponents) {
            ReleaseBundleQuery query = new ReleaseBundleQuery();
            query.setAql(specComponent.getAql());
            query.setAddedProps(specComponent.getAddedProps());
            query.setMappings(specComponent.getMappings());
            queries.add(query);
        }
        ReleaseBundleSpec results = new ReleaseBundleSpec();
        results.setQueries(queries);
        return results;
    }

    @Deprecated
    private static org.jfrog.filespecs.FileSpec toFileSpec(Spec spec) throws IOException {
        org.jfrog.filespecs.FileSpec response = new org.jfrog.filespecs.FileSpec();
        for (org.jfrog.build.extractor.clientConfiguration.util.spec.FileSpec file : spec.getFiles()) {
            Aql aql = new Aql();
            aql.setFind(file.getAql());
            FilesGroup filesGroup = new FilesGroup()
                    .setPattern(file.getPattern())
                    .setTarget(file.getTarget())
                    .setAql(aql)
                    .setTargetProps(file.getTargetProps())
                    .setSortBy(file.getSortBy())
                    .setSortOrder(file.getSortOrder())
                    .setOffset(file.getOffset())
                    .setLimit(file.getLimit());
            response.addFilesGroup(filesGroup);
        }
        return response;
    }
}
