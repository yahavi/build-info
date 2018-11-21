package org.jfrog.build.extractor.npm.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by Yahav Itzhak on 15 Nov 2018.
 */
public class NpmDriver implements Serializable {
    private static final long serialVersionUID = 1L;

    private static ObjectReader jsonReader = new ObjectMapper().reader();
    private String executablePath;

    public NpmDriver(String executablePath) {
        this.executablePath = StringUtils.defaultIfEmpty(executablePath, "npm");
    }

    /**
     * Execute a npm command.
     *
     * @param execDir - The execution dir (Usually path to project). Null means current directory.
     * @param args    - Command arguments.
     * @return NpmCommandRes
     */
    private NpmCommandRes exeNpmCommand(File execDir, List<String> args) throws InterruptedException, IOException {
        args.add(0, executablePath);
        Process process = null;
        ExecutorService service = Executors.newFixedThreadPool(2);
        try {
            NpmCommandRes npmCommandRes = new NpmCommandRes();
            process = exeCommand(execDir, args);
            StreamReader inputStreamReader = new StreamReader(process.getInputStream());
            StreamReader errorStreamReader = new StreamReader(process.getErrorStream());
            service.submit(inputStreamReader);
            service.submit(errorStreamReader);
            if (process.waitFor(30, TimeUnit.SECONDS)) {
                service.shutdown();
                service.awaitTermination(10, TimeUnit.SECONDS);
                npmCommandRes.res = inputStreamReader.getOutput();
                npmCommandRes.err = errorStreamReader.getOutput();
            } else {
                npmCommandRes.err = String.format("Process execution %s timed out.", String.join(" ", args));
            }
            npmCommandRes.exitValue = process.exitValue();

            return npmCommandRes;
        } finally {
            closeStreams(process);
            service.shutdownNow();
        }
    }

    private static void closeStreams(Process process) {
        if (process != null) {
            IOUtils.closeQuietly(process.getInputStream());
            IOUtils.closeQuietly(process.getOutputStream());
            IOUtils.closeQuietly(process.getErrorStream());
        }
    }

    public boolean isNpmInstalled() {
        List<String> args = Lists.newArrayList("version");
        try {
            NpmCommandRes npmCommandRes = exeNpmCommand(null, args);
            return npmCommandRes.isOk();
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    public void install(File workingDirectory, List<String> extraArgs) throws IOException {
        try {
            List<String> args = Lists.newArrayList(extraArgs);
            args.add(0, "i");
            NpmCommandRes npmCommandRes = exeNpmCommand(workingDirectory, args);
            if (!npmCommandRes.isOk()) {
                throw new IOException(npmCommandRes.err);
            }
        } catch (IOException | InterruptedException e) {
            throw new IOException("npm install failed: " + e.getMessage(), e);
        }
    }

    public JsonNode list(File workingDirectory, List<String> extraArgs) throws IOException {
        List<String> args = Lists.newArrayList("ls", "--json");
        args.addAll(extraArgs);
        try {
            NpmCommandRes npmCommandRes = exeNpmCommand(workingDirectory, args);
            String res = StringUtils.isBlank(npmCommandRes.res) ? "{}" : npmCommandRes.res;
            return jsonReader.readTree(res);
        } catch (IOException | InterruptedException e) {
            throw new IOException("npm ls failed", e);
        }
    }

    public String version(File workingDirectory) throws IOException, InterruptedException {
        List<String> args = Lists.newArrayList("-version");
        NpmCommandRes npmCommandRes = exeNpmCommand(workingDirectory, args);
        if (!npmCommandRes.isOk()) {
            throw new IOException(npmCommandRes.err);
        }
        return npmCommandRes.res;
    }

    public String configList(File workingDirectory, List<String> extraArgs) throws IOException, InterruptedException {
        List<String> args = Lists.newArrayList("c", "ls", "--json");
        args.addAll(extraArgs);
        NpmCommandRes npmCommandRes = exeNpmCommand(workingDirectory, args);
        if (!npmCommandRes.isOk()) {
            throw new IOException(npmCommandRes.err);
        }
        return npmCommandRes.res;
    }


    private static class NpmCommandRes {
        String res;
        String err;
        int exitValue;

        private boolean isOk() {
            return exitValue == 0;
        }
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    private static boolean isMac() {
        return System.getProperty("os.name").toLowerCase().contains("mac");
    }

    public static Process exeCommand(File execDir, List<String> args) throws IOException {
        String strArgs = String.join(" ", args);
        if (isWindows()) {
            return Runtime.getRuntime().exec(new String[]{"cmd", "/c", strArgs}, null, execDir);
        }
        if (isMac()) {
            return Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", strArgs}, new String[]{"PATH=$PATH:/usr/local/bin"}, execDir);
        }
        // Linux
        return Runtime.getRuntime().exec(args.toArray(new String[0]), null, execDir);
    }
}