package uk.ac.standrews.cs.descent.sed;

import org.json.JSONArray;
import org.json.JSONObject;
import uk.ac.standrews.cs.descent.Descent;
import uk.ac.standrews.cs.descent.daos.WikipediaGloveDataAccessObject;
import uk.ac.standrews.cs.descent.msed.MsedRep;
import uk.metricSpaceFramework.util.ConcurrentOrderedList;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class RunSedExperiments {
    static final int minSyns = 3;
    public static final String headerPath = "data/rowheaders.txt";
    private static final String filteredSynPath = "data/filtered_syns.json";

    public static final int num_nns = 100;

    /**
     * Fetches all headers. The words the vectors represent.
     * @return A list of words
     */
    static ArrayList<String> initHeaders() {
        ArrayList<String> lines = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(headerPath))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }

        return lines;
    }

    /**
     * Formats a json list for printing
     * @param li THe list to format
     * @return A list of strings
     */
    static List<String> jsonArrayToStringList(JSONArray li) {
        ArrayList<String> syns = new ArrayList<>();
        for (int i = 0; i < li.length(); i++) {
            syns.add(li.getString(i).replace('_', ' '));
        }
        return syns;
    }

    static HashMap<List<String>, Integer> getPositions(String word, List<String> syns, Descent<MsedRep> index, WikipediaGloveDataAccessObject context, List<String> headers) {
        int qid = headers.lastIndexOf(word);
        HashMap<List<String>, Integer> queryResults = new HashMap<>();
        int maxBitVal = (int) Math.pow(2, syns.size());

        for (int i = 1; i < maxBitVal; i++) {
            // Use the bits of i to select the points used
            String bits = Integer.toBinaryString(i);

            int bitLen = bits.length();
            // Prepad string
            for (int j = 0; j < syns.size() - bitLen; j++) {
                bits = '0' + bits;
            }

            ArrayList<String> queries = new ArrayList<>();
            for (int j = 0; j < bits.length(); j++) {
                if (bits.charAt(j) == '1') {
                    queries.add(syns.get(j));
                }
            }

            Set<Integer> sLi = new HashSet<>();
            for (String s : queries) {
                sLi.add(headers.lastIndexOf('\'' + s + '\''));
            }

            List<double[]> reps = new ArrayList<>();

            // Do the query
            for (int queryId : sLi) {
                reps.add(context.getData(queryId).getVecSum());
            }

            MsedRep query = new MsedRep(reps);

            ConcurrentOrderedList<Integer, Double> result = index.knnSearch(query, 100, 100);


            int queryPos = -1;

            for (int wordidx = 0; wordidx < 100; wordidx++) {
                if (result.get(wordidx).X() == qid) {
                    queryPos = wordidx;
                    break;
                }
            }


            // Removes any queries above the real answer
            for (String s : queries) {
                int sPos = headers.lastIndexOf('\'' + s + '\'');
                if (sPos > -1 && sPos < queryPos) {
                    queryPos--;
                }
            }

            queryResults.put(queries, queryPos);
        }

        return queryResults;
    }

    static JSONObject getPositionsTimingsRecalls(String word, List<String> syns, Descent<MsedRep> index, WikipediaGloveDataAccessObject context, List<String> headers) {
        int qid = headers.lastIndexOf(word);
        HashMap<List<String>, Integer> queryResults = new HashMap<>();
        int maxBitVal = (int) Math.pow(2, syns.size());
        JSONObject wordJson = new JSONObject();
        int count = 0;

        for (int i = 1; i < maxBitVal; i++) {
            JSONObject innerJson = new JSONObject();
            // Use the bits of i to select the points used
            String bits = Integer.toBinaryString(i);

            int bitLen = bits.length();
            // Prepad string
            for (int j = 0; j < syns.size() - bitLen; j++) {
                bits = '0' + bits;
            }

            ArrayList<String> queries = new ArrayList<>();
            for (int j = 0; j < bits.length(); j++) {
                if (bits.charAt(j) == '1') {
                    queries.add(syns.get(j));
                }
            }

            Set<Integer> sLi = new HashSet<>();
            for (String s : queries) {
                sLi.add(headers.lastIndexOf('\'' + s + '\''));
            }

            List<double[]> reps = new ArrayList<>();

            // Do the query
            for (int queryId : sLi) {
                reps.add(context.getData(queryId).getVecSum());
            }

            MsedRep query = new MsedRep(reps);

            long approxStartTimeMs = System.currentTimeMillis();
            ConcurrentOrderedList<Integer, Double> result = index.knnSearch(query, 100, 100);
            long approxEndTimeMs = System.currentTimeMillis();
            long approxTime = approxEndTimeMs - approxStartTimeMs;

            int queryPos = -1;
	    
	        System.out.println(result);
            for (int wordidx = 0; wordidx < 100; wordidx++) {
                if (result.get(wordidx).X() == qid) {
                    queryPos = wordidx;
                    break;
                }
            }

            // Removes any queries above the real answer
            for (String s : queries) {
                int sPos = headers.lastIndexOf('\'' + s + '\'');
                if (sPos > -1 && sPos < queryPos) {
                    queryPos--;
                }
            }

            long exactStartMs = System.currentTimeMillis();
            ConcurrentOrderedList<Integer, Double> exactResult = index.exactKnnSearch(query, 100, 100);
            long exactEndMs = System.currentTimeMillis();
            long exactTimeMs = exactEndMs - exactStartMs;

            // Adds the names of the queries used
            innerJson.put("queries", queries);
            // How many were used
            innerJson.put("number_query_objects", queries.size());
            // The position of the target word
            innerJson.put("target_position", queryPos);

            // The approximate recall compared to the exact recall
            innerJson.put("recall", getRecall(result, exactResult));

            // The time taken
            innerJson.put("approx_time", approxTime);
            innerJson.put("exact_time", exactTimeMs);

            wordJson.put(String.valueOf(count), innerJson);
            count++;
        }

        return wordJson;
    }

    private static int getRecall(ConcurrentOrderedList<Integer, Double> result, ConcurrentOrderedList<Integer, Double> exactResult) {
        int overlap = 0;
        for (int i : result.getValueCollection()) {
            if (exactResult.getValueCollection().contains(i))
                overlap++;
        }
        return overlap;
    }

    private static ArrayList<String> getOxford5000() throws IOException {
        // Loads the Oxford 5000
        String oxfordStr = Files.readString(new File("/home/bc89/IdeaProjects/WebImageProject/data/oxford-5000.json").toPath());
        JSONArray oxford5000 = new JSONArray(oxfordStr);
        ArrayList<String> oxfordList = new ArrayList<>();

        for (Object s : oxford5000) {
            JSONObject outer = new JSONObject(s.toString());
//            System.out.println(outer);
            JSONObject inner = outer.getJSONObject("value");
//            System.out.println(inner);
            oxfordList.add(inner.getString("word"));
        }

        return oxfordList;
    }

    static HashMap<String, List<String>> filterSyns(String path, HashMap<String, List<String>> syns, List<String> headers) throws IOException {
        File f = new File(filteredSynPath);
        HashMap<String, List<String>> result = new HashMap<>();

        if (f.isFile()) {
            System.out.println("Found filtered header file, loading...");

            // Load and return
            String fileString = Files.readString(f.toPath());
            JSONObject json = new JSONObject(fileString);

            for (String s : json.keySet()) {
                result.put(s, json.getJSONArray(s).toList().stream().map(Object::toString).collect(Collectors.toList()));
            }
        } else {
            System.out.println("Can't find file, filtering headers...");

            ArrayList<String> oxford5000 = getOxford5000();

            // Generate and return
            for (String word : syns.keySet()) {
                // Check word is in index
                int qid = headers.lastIndexOf('\'' + word + '\'');

                if (qid == -1 && oxford5000.lastIndexOf(word) != -1) {
                    continue;
                }

                ArrayList<String> filteredSyns = new ArrayList<>();

                // Gets only synonyms which are in the set
                for (String s : syns.get(word)) {
                    int sid = headers.lastIndexOf('\'' + s + '\'');

                    if (sid != -1 && oxford5000.lastIndexOf(word) != -1) {
                        filteredSyns.add(s);
                    }
                }

                // Check we have enough synonyms
                if (filteredSyns.size() < minSyns) {
                    continue;
                }
                result.put(word, filteredSyns);
                System.out.println("Put word " + word);
            }

            // Save to file
            JSONObject jsonWrite = new JSONObject();

            for (String w : result.keySet()) {
                jsonWrite.put(w, result.get(w));
            }

            PrintWriter out = new PrintWriter(path, "UTF-8");
            out.println(jsonWrite.toString(2));
            out.close();
        }
        return result;
    }

    public static void main(String[] args) throws IOException {
        JSONObject results = new JSONObject();
        HashMap<String, List<String>> wordSyns = readDataFile();

        String restore_indices_path = "data/wiki_glove_sed_nns_sm_t100_indices.txt";
        String restore_dists_path = "data/wiki_glove_sed_nns_sm_t100_dists.txt";

        WikipediaGloveDataAccessObject context = new WikipediaGloveDataAccessObject();

        Descent desc = new Descent(context, restore_indices_path, restore_dists_path, num_nns);

        ArrayList<String> headers = initHeaders();

        HashMap<String, List<String>> filteredSyns = filterSyns(
                "data/filtered_syns.json",
                wordSyns,
                headers);

        int numdone = 0;
        int numtodo = filteredSyns.keySet().size();

        for (String word : filteredSyns.keySet()) {
            List<String> syns = filteredSyns.get(word);

            if (syns.size() > 10) {
                syns = syns.subList(0, 10);
            }

            try {
                JSONObject currentResults = new JSONObject();
                System.out.println("Word " + word + " has " + syns.size() + " synonyms. Progress: " + numdone + "/" + numtodo );
                numdone++;
                JSONObject positions = getPositionsTimingsRecalls('\'' + word.toLowerCase() + '\'', syns, desc, context, headers);
                results.put(word, positions);
            } catch (RuntimeException e) {
                System.err.println(e);
		e.printStackTrace();
            }
        }

        // Write JSON out
        PrintWriter out = new PrintWriter("synonym_results.json", "UTF-8");
        out.println(results.toString(2));
        out.close();
    }

    private static HashMap<String, List<String>> readDataFile() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("data/en_thesaurus.jsonl"));

        String line = reader.readLine();
        int linesAccepted = 0;
        HashMap<String, List<String>> wordToSyns = new HashMap<>();


        while (line != null) {
            // Read JSON line
            JSONObject jsonLine = new JSONObject(line);
            JSONArray syns = jsonLine.getJSONArray("synonyms");
            String word = jsonLine.getString("word");

            if (syns.length() < minSyns) {
                line = reader.readLine();
                continue;
            }

            linesAccepted++;
            List<String> strSyns = jsonArrayToStringList(syns);

            wordToSyns.put(word, strSyns);
            line = reader.readLine();
        }

        System.out.println("Words accepted: " + linesAccepted);
        return wordToSyns;
    }
}
