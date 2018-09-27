package com.ca.apm.systemtest.fld.plugin.appdetect;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileWalker {
    private static final Logger log = LoggerFactory.getLogger(FileWalker.class);
    
	private List<Path> rootFolders = new ArrayList<>();
	private List<AppServerDetector> detectors = new ArrayList<>();

	public FileWalker() {
		for (File f : File.listRoots()) {
			rootFolders.add(Paths.get(f.getAbsolutePath()));
		}
	}

	public void registerDetector(AppServerDetector detector) {
		detectors.add(detector);
	}

	public void setRootFolders(Path... paths) {
		rootFolders = Arrays.asList(paths);
	}

	private void search(Path dir) {
		try {
			Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {

				@Override
				public FileVisitResult visitFile(Path file,
						BasicFileAttributes attrs) throws IOException {
					String fileString = file.toAbsolutePath().toString();

					for (AppServerDetector detector : detectors) {
						detector.testFile(fileString);
					}
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFileFailed(Path file, IOException e)
						throws IOException {
					log.info("Visiting failed for {}", file);

					return FileVisitResult.SKIP_SUBTREE;
				}
			});
		} catch (IOException e) {
		    log.error("Got exception during file search", e);
		}
	}

	public void startSearch() {
		if (detectors.isEmpty() || rootFolders.isEmpty()) {
			return;
		}

		for (Path dir : rootFolders) {
			search(dir);
		}
	}
}
