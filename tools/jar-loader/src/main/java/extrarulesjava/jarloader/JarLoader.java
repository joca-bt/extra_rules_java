package extrarulesjava.jarloader;

import extrarulesjava.jarloader.launch.Archive.Entry;
import extrarulesjava.jarloader.launch.ExecutableArchiveLauncher;

public class JarLoader extends ExecutableArchiveLauncher {
    private static final String JARS_DIR = "jars/";

    public JarLoader() throws Exception {}

    @Override
    protected String getEntryPathPrefix() {
        return JARS_DIR;
    }

    @Override
    protected boolean isIncludedOnClassPath(Entry entry) {
        String name = entry.name();
        return name.startsWith(JARS_DIR);
    }

	public static void main(String[] args) throws Exception {
        JarLoader jarLoader = new JarLoader();
        jarLoader.launch(args);
    }
}
