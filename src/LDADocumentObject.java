package parallelLDA;

import java.io.IOException;

/** @author adnan
 */
public class LDADocumentObject {
	private int[] words;
	private int[] topicOfWords;
	/** stores the distribution of topics associated with each doc*/
	private int[] topicdist;
	private int totalWords;
	
	/** if the topic proportion prior αlpha receives is relatively large, then many topics will 
	 * be activated per document. On the other hand if αlpha  is small (say 0.1), then only few 
	 * topics will be activated per document. If αlpha is almost  zero, each document would have only one topic
	 */
	private double alpha = 0.001;
	
	
	/** The value of β thus affects the granularity of the model: a corpus of documents can be sensibly factorized 
	 * into a set of topics at several different scales, and the particular scale assessed by the model will be set by 
	 * β. With scientific documents, a large value of β would lead the model to find a relatively small number of topics,
	 *  perhaps at the level of scientific disciplines, whereas smaller values of β will produce more topics that address 
	 *  specific areas of research.
	 */
	private double beta = 0.78;
	
	public LDADocumentObject (int[] wordSequences){
		this.words = wordSequences;
		this.topicOfWords = new int[words.length];
	}


	public void resampleTopics(int threadID, int partitionSize, int numOFTopics, int v, TopicCounter[] topicCounters) {
		TopicCounter[] threadSpecificLocalCounter = LDAParallelSampler.threadLocalDiffCounter.get();
		for (int i = 0 ; i < words.length;i++){
			if (words[i] % partitionSize == threadID){
			sampleFromConditional(i, threadSpecificLocalCounter,topicCounters);
			}
		}
		try {
			syncWithGlobalCounters(topicCounters,false,partitionSize,threadID);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	private void sampleFromConditional(int wordIndex,TopicCounter[] localCounters, TopicCounter[] topicCounters){
		int wordID = words[wordIndex];
		int oldtopic = topicOfWords[wordIndex];
		TopicCounter localCounter = localCounters[oldtopic];
		TopicCounter topicCounter = topicCounters[oldtopic];
		topicdist[oldtopic]--;
		localCounter.incrementwWordCount(wordID, -1);
		
		float[] prob = new float[topicdist.length];
		for (int i = 0; i < prob.length; i++){
			float freqOfWordInstanceInTopic =(float)(localCounter.getWordFreq(wordIndex)+ topicCounter.getWordFreq(wordIndex));
			float totalWordsIntopic = (float)(localCounter.getTotalWords()+ topicCounter.getTotalWords());
			
			prob[i] =  (float) ((freqOfWordInstanceInTopic+beta)/(totalWordsIntopic+LDAParallelSampler.v*beta) *(topicdist[i]+alpha)/(words.length+topicdist.length*alpha));
			}
		
		// scaled sample because of unnormalised p[]
		for (int i =1;i<prob.length;i++) prob[i]+=prob[i-1];
		double u = Math.random() * prob[prob.length-1];
        
        int topic = 0;
        for (topic = 0; topic < prob.length; topic++) {
        	if (u < prob[topic])
                break;
        }
       
        topicdist[topic]++;
        topicOfWords[wordIndex] = topic;
        localCounters[topic].incrementwWordCount(wordID,1);
        
	}
	
	public static void syncWithGlobalCounters(TopicCounter[] globalCounters, boolean sync, int numOfcpus,int threadID) throws IOException{
		TopicCounter[] localTopicThreadCounters = LDAParallelSampler.threadLocalDiffCounter.get();
		if (localTopicThreadCounters != null){
			for (int i = 0; i < globalCounters.length;i++){
				globalCounters[i].syncCounts(localTopicThreadCounters[i], sync, threadID, numOfcpus);
				//check whether sync has succeeded
				if (localTopicThreadCounters[i].getTotalWords()!=0){
					throw new IOException("Syncing between thread topic counters and gloabal topic counters failed");
				}
			}
		}
	}
/** randomly allocates each instance of a word in the document to a topic*/
	public void intialiseCounts(int numOfTopics, TopicCounter[] topicCounters) {
		topicdist = new int [numOfTopics];
		for (int i = 0; i< words.length;i++){
			int topic = (int) Math.floor(Math.random()*numOfTopics);
			topicCounters[topic].incrementwWordCount(words[i], 1);
			topicOfWords[i] = topic;
			topicdist [topic]+=1;
		}
		
	}
	
	public float[] returnTopicdist(){
		float[] topicD = new float[topicdist.length];
		for (int i =0; i < topicD.length;i++)topicD[i] = (float)topicdist[i]/words.length;
		return topicD;
	}
	
}
