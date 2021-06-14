package org.jfrog.build.extractor.clientConfiguration.client;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.jfrog.build.IntegrationTestsBase;
import org.jfrog.build.client.ArtifactoryUploadResponse;
import org.jfrog.build.client.Version;
import org.jfrog.build.extractor.clientConfiguration.client.distribution.DistributionManager;
import org.jfrog.build.extractor.clientConfiguration.client.distribution.request.CreateReleaseBundleRequest;
import org.jfrog.build.extractor.clientConfiguration.client.distribution.request.UpdateReleaseBundleRequest;
import org.jfrog.build.extractor.clientConfiguration.client.distribution.types.ReleaseNotes;
import org.jfrog.build.extractor.clientConfiguration.deploy.DeployDetails;
import org.jfrog.build.extractor.clientConfiguration.util.spec.FileSpec;
import org.jfrog.build.extractor.clientConfiguration.util.spec.Spec;
import org.testng.annotations.*;
import org.testng.collections.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

import static org.testng.Assert.assertFalse;

/**
 * @author yahavi
 **/
public class DistributionManagerTest extends IntegrationTestsBase {
    static final String TEST_SPACE = "bi_dist_client_test_space";
    static final File tempWorkspace = new File(System.getProperty("java.io.tmpdir"), TEST_SPACE);
    static final String RELEASE_BUNDLE_NAME = "bitests-dist-name";
    static final String RELEASE_BUNDLE_VERSION = String.valueOf(new Date().getTime());
    DistributionManager distributionManager;

    @BeforeClass
    @Override
    public void init() throws IOException {
        super.init();
        String distributionUrl = getPlatformUrl() + "/distribution";
        distributionManager = new DistributionManager(distributionUrl, getUsername(), getPassword(), "", getLog());
        createTestRepo(localRepo2);
    }

    @AfterClass
    @Override
    public void terminate() throws IOException {
        deleteTestRepo(localRepo2);
        super.terminate();
    }

    @BeforeMethod
    public void setUp() throws IOException {
        deleteContentFromRepo(localRepo1);
        deleteContentFromRepo(localRepo2);
        Files.createDirectories(tempWorkspace.toPath());
    }

    @AfterMethod
    protected void cleanup() throws IOException {
        FileUtils.deleteDirectory(tempWorkspace);
        deleteContentFromRepo(localRepo1);
        deleteContentFromRepo(localRepo2);
    }

    @Test
    public void getVersionTest() throws IOException {
        Version version = distributionManager.getVersion();
        assertFalse(version.isNotFound());
    }

    CreateReleaseBundleRequest.Builder createRequestBuilder() throws IOException {
        Spec releaseBundleSpec = createSpec();
        ReleaseNotes releaseNotes = new ReleaseNotes();
        releaseNotes.setContent("Create content");
        releaseNotes.setSyntax(ReleaseNotes.Syntax.plain_text);
        return (CreateReleaseBundleRequest.Builder) new CreateReleaseBundleRequest.Builder(RELEASE_BUNDLE_NAME, RELEASE_BUNDLE_VERSION)
                .description("Create")
                .releaseNotes(releaseNotes)
                .spec(releaseBundleSpec);
    }

    UpdateReleaseBundleRequest.Builder<UpdateReleaseBundleRequest> updateRequestBuilder() throws IOException {
        Spec releaseBundleSpec = createSpec();
        ReleaseNotes releaseNotes = new ReleaseNotes();
        releaseNotes.setContent("Update content");
        releaseNotes.setSyntax(ReleaseNotes.Syntax.plain_text);
        return new UpdateReleaseBundleRequest.Builder<>().description("Update").releaseNotes(releaseNotes).spec(releaseBundleSpec);
    }

    Spec createSpec() throws IOException {
        String fileName = uploadFile();
        FileSpec filesGroup = new FileSpec();
        filesGroup.setTargetProps("key1=value1,value2");
        filesGroup.setPattern(localRepo1 + "/data/" + fileName);
        Spec fileSpec = new Spec();
        fileSpec.setFiles(new FileSpec[]{filesGroup});
        return fileSpec;
    }

    String uploadFile() throws IOException {
        String fileName = RandomStringUtils.randomAlphabetic(10);
        Path tmpFile = tempWorkspace.toPath().resolve(fileName).toAbsolutePath();
        Files.createFile(tmpFile);
        Files.write(tmpFile, RandomStringUtils.randomAlphabetic(10).getBytes(StandardCharsets.UTF_8));
        DeployDetails deployDetails = new DeployDetails.Builder()
                .file(tmpFile.toFile())
                .artifactPath("data/" + fileName)
                .targetRepository(localRepo1)
                .build();
        ArtifactoryUploadResponse response = artifactoryManager.upload(deployDetails);
        assertFalse(CollectionUtils.hasElements(response.getErrors()));
        return fileName;
    }
}
