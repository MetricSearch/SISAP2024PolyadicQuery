# Wikipedia Glove Experiments
This reporitory contains the experiments detailed in "Scalable Polyadic Queries" for SISAP2024.

## Running Experiments
### Generating Near Neighbour Graphs

Navigate and run "src/python/make_sed_nns.py" in the Python folder. This will build the near neighbour table using the SED metric. The path within the program will need to be changed to point to a .MAT version of 100D Wikipedia GloVe. This can be produced by opening the raw txt downloadable at https://nlp.stanford.edu/projects/glove/ in Matlab and saving as a .MAT file.

Running this file will create three files: wiki_glove_sm_t10, wiki_glove_sed_nns_sm_t10_dists.txt and wiki_glove_sed_nns_sm_t10_indices.txt. These should be placed in the data folder. All other necessary files are in this folder. A description of these files and their providence follows:

en_thesaurus.jsonl is the list of Wordnet synonyms as downloaded from https://github.com/zaibacu/thesaurus. This list can be extracted from the 5th entry in the JSONL file. Only words with 3+ synonyms, all of which were in the Oxford 5000 (json file obtained from https://github.com/tyypgzl/Oxford-5000-words), were retained from the Wordnet set. The results of this process are stored in filtered_syns.json.

### Running Timing Experiments
To run the timing experiments, run the main method found in uk.ac.standrews.cs.descent.sed.RunMSEDExperiments, ensuring that all data files are in the data folder as described above. This will create a file on the top level called "synonym_results.json", which contains details reported upon in the paper of each experiment.

Our version of the data is contained in synonym_results.json.

### Rebuilding Results Graphs
Run "src/python/analyse_results.ipynb". All graphs will be regenerated from the JSON file produced in the above step.