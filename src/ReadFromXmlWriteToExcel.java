import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;


public class ReadFromXmlWriteToExcel {
	FileInputStream sourceFile = null;
	FileOutputStream targetFile = null;
	
	HashSet trees = new HashSet();
	
	List history = new ArrayList();
	
	public ReadFromXmlWriteToExcel(String bazaarHistoryxml, String targetxml) throws Exception {
		sourceFile = new FileInputStream(bazaarHistoryxml);
		targetFile = new FileOutputStream(targetxml);
	}
	
	private void makeTree(BazaarLog log, Element node) {
		List childNode = node.getChildren();
		for( int cnt = 0 ; cnt < childNode.size() ; cnt++ ) {
			if (  childNode.get(cnt) instanceof Element ) {
				Element childElement = (Element)childNode.get(cnt);
				String nodeName = childElement.getName();
				if ( nodeName.equals("affected-files") ) {
					Element added = childElement.getChild("added");
					if ( added != null ) {
						List addedList = added.getChildren();
						for ( int addCount = 0 ; addCount < addedList.size() ; addCount++ ) {
							if ( addedList.get(addCount) instanceof Element ) {
								Element addfile = (Element)addedList.get(addCount);
								log.addedFiles.add(addfile.getText());
							}
						}
					}
					Element modified = childElement.getChild("modified");
					if ( modified != null ) {
						List modifiedList = modified.getChildren();
						for ( int modifiedCount = 0 ; modifiedCount < modifiedList.size() ; modifiedCount++ ) {
							if ( modifiedList.get(modifiedCount) instanceof Element ) {
								Element tempfile = (Element)modifiedList.get(modifiedCount);
								log.modifiedFile.add(tempfile.getText());
							}
						}
					}
					Element removed = childElement.getChild("removed");
					if ( removed != null ) {
						List removedList = removed.getChildren();
						for ( int removedCount = 0 ; removedCount < removedList.size() ; removedCount++ ) {
							if ( removedList.get(removedCount) instanceof Element ) {
								Element tempfile = (Element)removedList.get(removedCount);
								log.removedFile.add(tempfile.getText());
							}
						}
					}
				} else if ( nodeName.equals("branch-nick") ) {
					log.branch_nick = childElement.getText();
				} else if ( nodeName.equals("committer") ) {
					log.committer = childElement.getText();
				} else if ( nodeName.equals("message") ) {
					log.message = childElement.getText();
				} else if ( nodeName.equals("revno") ) {
					log.revno = childElement.getText();
				} else if ( nodeName.equals("merge") ) {
					BazaarLog newLog = new BazaarLog();
					log.mergeChildren.add(newLog);
					newLog.parent = log;
					makeTree( newLog, childElement.getChild("log") );
				} else if ( nodeName.equals("tags") ) {
					List tagList = childElement.getChildren("tag");
					if ( tagList != null ) {
						for ( int tagCount = 0 ; tagCount < tagList.size() ; tagCount++ ) {
							log.tagList.add(((Element)tagList.get(tagCount)).getText());
						}
					}
				} else if ( nodeName.equals("timestamp") ) {
					log.timestamp = childElement.getText();
				}
			}
		}
	}
	
	
	
	public void parseAndSave() throws Exception {
		SAXBuilder builder = new SAXBuilder();
		Document document = builder.build(sourceFile);
		Element root = document.getRootElement();
		List list = root.getChildren("log");
		for (int count = 0 ; count < list.size() ; count++ ) {
			System.out.println("line number:" + count);
			Element log = (Element)list.get(count);
			BazaarLog newLog = new BazaarLog();
			makeTree(newLog, log);
			history.add(newLog);
		}
	}
	
	public void printTrees() {
		Iterator iter = trees.iterator();
		while( iter.hasNext() ) {
			String tagPath = (String)iter.next();
			System.out.println("full_path:" + tagPath);
		}
	}
	
	public void printMergeHistory(BazaarLog log, int depth) {
		System.out.println(depth + ":revno:" + log.revno);
		printOneLogToExcel(log, depth);
		for( int cnt = 0; cnt < log.mergeChildren.size() ; cnt++ ) {
			printMergeHistory((BazaarLog)log.mergeChildren.get(cnt), depth+1);
		}
	}
	
	static int excelRowCount = 0;
    Workbook wb;
    Sheet sheet = null;
	
	public void initiailizeExcel() {
		excelRowCount = 0;
        wb = new HSSFWorkbook();
        sheet = wb.createSheet("History");
	}
	
	public static String makeStringFromList(List list) {
		StringBuffer sb = new StringBuffer();
		for ( int cnt = 0 ; list != null && cnt < list.size() ; cnt++ ) {
			sb.append(",").append(list.get(cnt));
		}
		return ( sb.length() > 0  ) ? sb.substring(1) : "";
	}
	
	public void printOneLogToExcel(BazaarLog log, int depth) {
        Row row;
        Cell cell;
        row = sheet.createRow(++excelRowCount);
        
        cell = row.createCell(depth);
        cell.setCellValue(log.revno);
        cell = row.createCell(depth+1);
        cell.setCellValue(makeStringFromList(log.tagList));

        int columnCnt = 21;
        //
        cell = row.createCell(columnCnt++);
        cell.setCellValue(log.branch_nick);
        cell = row.createCell(columnCnt++);
        cell.setCellValue(log.message);
        cell = row.createCell(columnCnt++);
        cell.setCellValue(log.committer);
        cell = row.createCell(columnCnt++);
        cell.setCellValue(log.timestamp);
        

	}
	
	void closeExcel() {
		try {
	        wb.write(targetFile);
	        targetFile.close();
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}
	
	public void printHistory() {
		initiailizeExcel();
		for( int cnt = 0 ; cnt < history.size() ; cnt++ ) {
			printMergeHistory((BazaarLog)history.get(cnt),0);
		}
		closeExcel();
	}
	
	public static void main(String[] args) throws Exception {
		ReadFromXmlWriteToExcel reader = new ReadFromXmlWriteToExcel("D:\\oss\\mariadb\\5.5\\trunk\\full_history.euckr.xml", "D:\\oss\\mariadb\\5.5\\trunk\\full_history.xls");
		reader.parseAndSave();
		reader.printHistory();
	}
	
}
