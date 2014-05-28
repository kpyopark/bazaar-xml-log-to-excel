import java.util.ArrayList;
import java.util.List;


public class BazaarLog {
	String affected_files;
	List addedDirectory = new ArrayList();
	List addedFiles = new ArrayList();
	List modifiedFile = new ArrayList();
	List removedDirectory = new ArrayList();
	List removedFile = new ArrayList();
	List renamedDirectory = new ArrayList();
	List renamedFile = new ArrayList();
	
	String branch_nick;
	String committer;
	BazaarLog parent;
	List mergeChildren = new ArrayList();
	String message;
	String revno;
	List tagList = new ArrayList();
	String timestamp;
}
