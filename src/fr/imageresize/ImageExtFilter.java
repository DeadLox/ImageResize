package fr.imageresize;

import java.io.File;
import java.io.FilenameFilter;

public class ImageExtFilter implements FilenameFilter {
	private String[] listeExtensions;
	
	public ImageExtFilter(String[] listExt){
		listeExtensions = listExt;
	}

	@Override
	public boolean accept(File folder, String fileName) {
		for (String ext : listeExtensions) {
			String fileNameLower = fileName.toLowerCase();
			if (fileNameLower.contains(ext)) {
				return true;
			}
		}
		return false;
	}

}
