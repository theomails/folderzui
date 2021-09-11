package net.progressit.folderzui.model;

import java.io.IOException;
import java.nio.file.FileSystemLoopException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

import com.google.common.eventbus.EventBus;

import lombok.Builder;
import lombok.Data;
import net.progressit.folderzui.model.Scanner.FolderDetails;

public class SizingFileVisitor implements FileVisitor<Path> {
	public enum SFVProblemType {FINE, WARN, ERROR}
	
	@Data
	public static class SFVFolderStartEvent{
		private final Path folder;
		private final FolderDetails folderDetails;
	}
	@Data
	@Builder
	public static class SFVFolderEndEvent{
		private final Path folder;
	}
	@Data
	public static class SFVFileVisitEvent{
		private final Path file;
	}
	
	@Data
	public static class SFVProblemEvent{
		private final SFVProblemType type;
		private final String message;
		private final Exception exception;
		public static SFVProblemEvent fine(String message) {
			return new SFVProblemEvent(SFVProblemType.FINE, message, null);
		}
		public static SFVProblemEvent warn(String message) {
			return new SFVProblemEvent(SFVProblemType.WARN, message, null);
		}
		public static SFVProblemEvent error(String message, Exception e) {
			return new SFVProblemEvent(SFVProblemType.ERROR, message, e);
		}
	}
	

	private final EventBus eventBus = new EventBus();
	private final Path rootFolder;
	private FolderDetails curDetails;
	private final Map<Path, FolderDetails> allDetailsContainer;

	public SizingFileVisitor(Path rootFolder, Map<Path, FolderDetails> allDetailsContainer, Object listener) {
		eventBus.register(listener);
		this.rootFolder = rootFolder;
		this.allDetailsContainer = allDetailsContainer;
	}

	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
		Path dirAbs = dir.toAbsolutePath();
		if(allDetailsContainer.containsKey(dirAbs) && !allDetailsContainer.containsKey(dir)) {
			eventBus.post(SFVProblemEvent.warn("Gets called for same absolute path with another path"));
		}
		
		curDetails = allDetailsContainer.get(dir);
		if (curDetails == null) {
			curDetails = new FolderDetails(dir);
			allDetailsContainer.put(dir, curDetails);
			eventBus.post(new SFVFolderStartEvent(dir, curDetails));
		}else {
			eventBus.post(SFVProblemEvent.warn("Same folder visited again"));
		}
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		if(file.toFile().isDirectory()) return FileVisitResult.CONTINUE;
		
		if (attrs.isSymbolicLink()) {
			eventBus.post(SFVProblemEvent.fine("Symbolic link: " + file));
		} else if (attrs.isRegularFile()) {

		} else {
			eventBus.post(SFVProblemEvent.fine("Not regular file: " + file));
		}

		
		long size = attrs.size();
		curDetails.addFileSize(file, size);
		eventBus.post(new SFVFileVisitEvent(file));
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
		if (exc instanceof FileSystemLoopException) {
			eventBus.post(SFVProblemEvent.warn("Cycle detected: " + file));
		} else {
			eventBus.post(SFVProblemEvent.error("Unable to open folder:" + file, exc));
		}
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
		if (!dir.equals(rootFolder)) {
			Path parent = dir.getParent();
			FolderDetails parentDetails = allDetailsContainer.get(parent);
			// Assumes all sub-children have been processed before postVisit of this child
			// directory. So, depends on it being Depth-first.
			parentDetails.cumulate(curDetails);
			curDetails = parentDetails;
		}else {
			FolderDetails rootFinalDetails = allDetailsContainer.get(dir);
			//rootFinalDetails.printNested( System.out );
			
		}
		eventBus.post(new SFVFolderEndEvent(dir));
		return FileVisitResult.CONTINUE;
	}

}