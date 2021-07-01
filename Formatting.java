import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Formatting {
	
	//If file does not exist, bw writes in Finder eclipse-workspace folder
	public static void toDOTformat(long[][] happensBefore, ArrayList<String> eventIDs) throws IOException {
		// https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(new File("DOT_formatted_trace.txt")))) {
		bw.write("digraph DAG {");
		bw.newLine();
		for(int i=0; i<happensBefore.length; i++) {
			for(int j=0; j<happensBefore[i].length; j++) {
				if(happensBefore[i][j]==1) {
					bw.write("\""+eventIDs.get(i)+"\""+"->"+"\""+eventIDs.get(j)+"\"");
					bw.newLine();
				}
			}
		}
		bw.write("}");
		bw.newLine();
		bw.close();
		}
	}
	
	public static void toPythonTupleList(long[][] happensBefore, ArrayList<String> eventIDs) {
		boolean first = true;
		System.out.print("[");
		for(int i=0; i<happensBefore.length; i++) {
			for(int j=0; j<happensBefore[i].length; j++) {
				if(happensBefore[i][j] == 0) {
					if(!first) {
						System.out.println("  , '\"\"'),  ");
					}
					else first = false;
					System.out.print("((\""+eventIDs.get(i)+ "\"" + ","+"\""+eventIDs.get(j)+"\""+")");
				}
			}
		}
		System.out.println("  , '\"\"')]");
	}
}
