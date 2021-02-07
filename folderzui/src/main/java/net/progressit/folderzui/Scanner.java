package net.progressit.folderzui;

import java.io.IOException;
import java.nio.file.FileSystemLoopException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import lombok.Data;

public class Scanner {

	@Data
	public static class FolderDetails {
		private final Path path;
		private long size = 0l;
		private long fullSize = 0l;
		private final Map<String, Long> typesSize = new HashMap<>();
		private final Map<String, Long> typesFullSize = new HashMap<>();
		private final Map<Path, FolderDetails> children = new LinkedHashMap<>();

		public void addFileSize(Path path, long addSize) {
			if(path.toFile().isDirectory()) {
				System.out.println("Reported as file: " + path);
			}
			
			Path fileAlone = path.getName(path.getNameCount() - 1);
			String fileAloneStr = fileAlone.toString();
			int lastIndex = fileAloneStr.lastIndexOf(".");
			String extn;
			if (lastIndex > 0) { // Note: we dont want files which only start with a dot
				extn = fileAloneStr.substring(lastIndex+1);
			} else {
				extn = "File";
			}
			size += addSize;
			fullSize += addSize;
			safeAdd(extn, addSize, typesSize);
			safeAdd(extn, addSize, typesFullSize);
		}

		public void cumulate(FolderDetails childCompleteDetails) {
			//System.out.println("Loading " + childCompleteDetails.path + " into " + path);
			
			fullSize += childCompleteDetails.fullSize;
			Set<String> childTypes = childCompleteDetails.typesFullSize.keySet();
			for (String childType : childTypes) {
				long childTypeFullSize = childCompleteDetails.typesFullSize.get(childType);
				safeAdd(childType, childTypeFullSize, typesFullSize);
			}
			children.put(childCompleteDetails.path, childCompleteDetails);
		}

		private void safeAdd(String key, long addVal, Map<String, Long> map) {
			Long nowVal = map.get(key);
			nowVal = (nowVal == null) ? 0L : nowVal;
			nowVal += addVal;
			map.put(key, nowVal);
		}

		@Override
		public String toString() {
			return "FolderDetails [\npath=" + path + ", \nsize=" + size + ", \nfullSize=" + fullSize + ", \ntypesSize="
					+ typesSize + ", \ntypesFullSize=" + typesFullSize + "]";
		}
		
	}

	public FolderDetails scan(Path folder) throws IOException {
		Map<Path, FolderDetails> allDetails = new HashMap<>();
		FolderDetails root = new FolderDetails(folder);
		allDetails.put(folder, root);

		// Needs to be depth first. Else the cumulation wont work.
		Files.walkFileTree(folder, new SizingFileVisitor(folder, allDetails));
		return root;
	}

	private static class SizingFileVisitor implements FileVisitor<Path> {

		private final Path startFolder;
		private FolderDetails curDetails;
		private final Map<Path, FolderDetails> allDetails;

		public SizingFileVisitor(Path startFolder, Map<Path, FolderDetails> allDetails) {
			this.startFolder = startFolder;
			this.allDetails = allDetails;
		}

		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
			curDetails = allDetails.get(dir);
			if (curDetails == null) {
				curDetails = new FolderDetails(dir);
				allDetails.put(dir, curDetails);
			}
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			if (attrs.isSymbolicLink()) {
//				System.out.format("Symbolic link: %s ", file);
			} else if (attrs.isRegularFile()) {

			} else {
//				System.out.format("Other: %s ", file);
			}

			
			long size = attrs.size();
			curDetails.addFileSize(file, size);
			
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
			if (exc instanceof FileSystemLoopException) {
//				System.err.println("cycle detected: " + file);
			} else {
//				System.err.format("Unable to copy:" + " %s: %s%n", file, exc);
			}
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
//			System.out.println(curDetails);
			if (!dir.equals(startFolder)) {
				Path parent = dir.getParent();
				FolderDetails parentDetails = allDetails.get(parent);
				// Assumes all sub-children have been processed before postVisit of this child
				// directory. So, depends on it being Depth-first.
				parentDetails.cumulate(curDetails);
				curDetails = parentDetails;
			}
			return FileVisitResult.CONTINUE;
		}

	}
}
