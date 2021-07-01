/*
Mystery Machine Replica - https://research.fb.com/wp-content/uploads/2016/11/the-mystery-machine-end-to-end-performance-analysis-of-large-scale-internet-services.pdf
Tanmay Gupta
7/1/21
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class Final_Mystery_Machine_Replica {

	public static void main(String[] args) throws IOException {
		Scanner s = null;
		try {
			
		// Reads JSON
		String content = "";
		try( BufferedReader br = new BufferedReader(new FileReader("json_test_deathstar.in")) ) {
			String line = br.readLine();
			while(line != null) {
				content += line;
				line = br.readLine();
			}
		}

		// Parses JSON
		JSONObject jsonObject = (JSONObject) JSONValue.parse(content);
		JSONArray traces = (JSONArray) jsonObject.get("data");
		int numTraces = traces.size();
		int numSpans = (  (JSONArray) ((JSONObject)traces.get(0)).get("spans")   ).size(); 
		
		Set<String> nameIDs = new HashSet<>(); // NameID = operationName + processID
		ArrayList<String> eventIDs = new ArrayList<>(); // each span will have its start and end timestamps become events
		// EventIDs not used in the algorithm, but used for display
		
		// In different traces, the order of the spans could be different and multiple spans could have the same name
		// so the maps make sure the happens-before matrix is updated in the right spot
		Map<String, Integer> spanIndices = new HashMap<String, Integer>();
		Map<String, String> nameIDToSpanID = new HashMap<String, String>();
		
		for(int i=0; i<traces.size(); i++) {
			for(int j=0; j<numSpans; j++) {
				JSONObject jthSpan = (JSONObject) (   (  (JSONArray) ((JSONObject)traces.get(i)).get("spans")  ).get(j)   );
				String name = (String) jthSpan.get("operationName") + (String) jthSpan.get("processID");
				String curSpanID = (String) jthSpan.get("spanID");
				if(nameIDs.contains(name)) {
					spanIndices.put(curSpanID, spanIndices.get(nameIDToSpanID.get(name)));
				}
				else {
					spanIndices.put((String) jthSpan.get("spanID"), nameIDs.size());
					nameIDs.add(name);
					nameIDToSpanID.put(name, curSpanID);
					eventIDs.add( name + "_start" );
					eventIDs.add( name + "_end" );
				}
			} 
		}
		
		// Makes fully connected happens-before graph
		long[][] happensBefore = new long[nameIDs.size()*2][nameIDs.size()*2]; // happensBefore[m][n] = 1 if m happens before n
		for(int r=0; r<happensBefore.length; r++) for(int c=0; c<happensBefore[r].length; c++) happensBefore[r][c] = 1;

		for(int j=0; j<nameIDs.size(); j++) {
			happensBefore[2*j+1][2*j] = 0; // End event does not happen before start event
			happensBefore[2*j][2*j] = 0; // No event happens before itself
			happensBefore[2*j+1][2*j+1] = 0;
		}
		
		// Testing
		//	System.out.println("numTraces: " + numTraces);
		//	System.out.println("numSpans: " + numSpans);
		//	System.out.println("numNames: " + nameIDs.size());
		//	System.out.println("NameIDs: " + nameIDs);
		//	System.out.println("Names->SpanIDs: "+ nameIDToSpanID);
		//	System.out.println("EventIDs: " + eventIDs);
		//	System.out.println("SpanIndices: " + spanIndices);
		
		// Loop through traces and update happens-before graph
		for(int i=0; i<numTraces; i++) {
			JSONObject curTrace = (JSONObject) traces.get(i);
			JSONArray traceSpans = (JSONArray) curTrace.get("spans");
			for(int j=0; j<numSpans-1; j++) { // do not want to include last one
				JSONObject spanA = (JSONObject) traceSpans.get(j);
				if(spanA == null) continue; // Handles case where span not present in one trace, but is present in other traces
				int A_Index = spanIndices.get(spanA.get("spanID"));
				long A_Start = (long)spanA.get("startTime");
				long A_End = ( (long)spanA.get("startTime") + (long)spanA.get("duration") );
				for(int k=j+1; k<numSpans; k++) {
					JSONObject spanB = (JSONObject) traceSpans.get(k);
					if(spanB == null) continue;
					int B_Index = spanIndices.get(spanB.get("spanID"));
					long B_Start = (long)spanB.get("startTime");
					long B_End = ( (long)spanB.get("startTime") + (long)spanB.get("duration") );
					// ALREADY DONE:
					// A_End -> A_Start - Always violated
					// A_Start -> A_Start - Always violated
					// A_End -> A_End - Always violated
					// B_End -> B_Start - Always violated
					// B_Start -> B_Start - Always violated
					// B_End -> B_End - Always violated

					// A_Start -> A_End - Never violated
					// B_Start -> B_End - Never violated

					
					// Below statements update the happens-before matrix if there are any violations
					// A_Start -> B_Start
					if(B_Start <= A_Start) happensBefore[2*A_Index][2*B_Index] = 0;
					// B_Start -> A_Start
					if(A_Start <= B_Start) happensBefore[2*B_Index][2*A_Index] = 0;
					// A_End -> B_End
					if(B_End <= A_End) happensBefore[2*A_Index+1][2*B_Index+1] = 0;
					// B_End -> A_End
					if(A_End <= B_End) happensBefore[2*B_Index+1][2*A_Index+1] = 0;
					// A_Start -> B_End
					if(B_End <= A_Start) happensBefore[2*A_Index][2*B_Index+1] = 0;
					// B_Start -> A_End
					if(A_End <= B_Start) happensBefore[2*B_Index][2*A_Index+1] = 0;
					// A_End -> B_Start
					if(B_Start <= A_End) happensBefore[2*A_Index+1][2*B_Index] = 0;
					// B_End -> A_Start
					if(A_Start <= B_End) happensBefore[2*B_Index+1][2*A_Index] = 0;	
				}
			}
		}
		
		// Print happens-before Matrix
		System.out.println("Happens-Before Causal Model:");
		System.out.println(Arrays.deepToString(happensBefore)); // adjacency matrix for directed graph where edges represent the "happens before relation."
		System.out.println();
		
		// Transitive Reduction
		System.out.println("After Transitive Reduction:");
		happensBefore = Transitive_Reduction_Algorithm.transitiveReduction(happensBefore);
		System.out.println(Arrays.deepToString(happensBefore));	
		
		// Write to DOT format file
		System.out.println("Erase and replace current contents of \"DOT_formatted_trace.txt\" ");
		String input = "";
		s = new Scanner(System.in);
		while(!(input.equals("y") || input.equals("n"))) {
			System.out.println("[y/n]");
			input = s.nextLine();
		}
		if(input.contentEquals("y")) {
			try {
				System.out.println(Arrays.deepToString(happensBefore));
				Formatting.toDOTformat(happensBefore, eventIDs);
				System.out.println("DOT formatted graph written to \"DOT_formatted_trace.txt\"");
			} catch (IOException e) {
				System.out.println("Error  writing to \"DOT_formatted_trace.txt\"");
				e.printStackTrace();
			}
		}
		} finally {
			if(!(s==null)) s.close();
		}
		
	}


}
