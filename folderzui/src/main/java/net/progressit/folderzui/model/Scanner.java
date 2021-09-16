package net.progressit.folderzui.model;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
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
		private long count = 0l;
		private long fullCount = 0l;
		private final Map<String, Long> typeSizes = new HashMap<>();
		private final Map<String, Long> typeFullSizes = new HashMap<>();
		private final Map<Path, FolderDetails> childrenDetails = new LinkedHashMap<>();

		public void addFileSize(Path path, long addSize) {
			if(path.toFile().isDirectory()) {
				System.out.println("WARN: Reported as file: " + path);
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
			count++;
			fullCount++;
			safeAdd(extn, addSize, typeSizes);
			safeAdd(extn, addSize, typeFullSizes);
		}

		public void cumulate(FolderDetails childCompleteDetails) {
			//System.out.println("Loading " + childCompleteDetails.path + " into " + path);
			
			fullSize += childCompleteDetails.fullSize;
			fullCount += childCompleteDetails.fullCount;
			Set<String> childTypes = childCompleteDetails.typeFullSizes.keySet();
			for (String childType : childTypes) {
				long childTypeFullSize = childCompleteDetails.typeFullSizes.get(childType);
				safeAdd(childType, childTypeFullSize, typeFullSizes);
			}
			childrenDetails.put(childCompleteDetails.path, childCompleteDetails);
		}

		private void safeAdd(String key, long addVal, Map<String, Long> map) {
			Long nowVal = map.get(key);
			nowVal = (nowVal == null) ? 0L : nowVal;
			nowVal += addVal;
			map.put(key, nowVal);
		}

		@Override
		public String toString() {
			return "FolderDetails [ path=" + path + ", size=" + size + ", fullSize=" + fullSize + ", count=" + count + ", fullCount=" + fullCount + ", \n typeSizes="
					+ typeSizes + ", \n typeFullSizes=" + typeFullSizes + "]";
		}

		public void printNested(PrintStream ps) {
			printNested(ps, this, 0);
		}
		private static void printNested(PrintStream ps, FolderDetails folder, int level) {
			StringBuilder sb = new StringBuilder(1000);
			String prefix = "";
			for(int i=0;i<level;i++) {
				prefix += "  ";
			}
			sb.append(prefix).append("FolderDetails [ path=").append(folder.path).append(", size=").append(folder.size).append(", fullSize=").append(folder.fullSize);
			ps.println(sb.toString());
			sb.setLength(0);
			
			sb.append(prefix).append("| typeSizes=").append(folder.typeSizes);
			ps.println(sb.toString());
			sb.setLength(0);
			
			sb.append(prefix).append("| typeFullSizes=").append(folder.typeFullSizes);
			ps.println(sb.toString());
			sb.setLength(0);
			
			Collection<FolderDetails> childrenDetails = folder.getChildrenDetails().values();
			for(FolderDetails childDetails:childrenDetails) {
				printNested(ps, childDetails, level+1);
			}
		}
	}

	public void scan(Path rootFolder, Map<Path, FolderDetails> allDetailsContainer, Object listener) throws IOException {
		// Needs to be depth first. Else the cumulation wont work.
		Files.walkFileTree(rootFolder, new SizingFileVisitor(rootFolder, allDetailsContainer, listener));
	}
}
