package parallelLDA;



public class LDAParallelSampler {
	/** vocabulary size**/
	static int v;
	
	static TopicCounter[]  topicCounters;
	/** number of topics**/
	static int numOFTopics;
	
	public static ThreadLocal<TopicCounter[]> threadLocalDiffCounter =	new ThreadLocal<TopicCounter[]>() {
	            protected TopicCounter[] initialValue() {
	            	TopicCounter[] counters = new TopicCounter[numOFTopics];
	                for(int i=0; i<numOFTopics; i++) counters[i] = new TopicCounter(v);
	                threadLocalDiffCounter.set(counters);
	                return threadLocalDiffCounter.get();
	             }
	};
	


	
	public static void sample(java.util.List<LDADocumentObject> list, int vMod, int modulo) {
		for (LDADocumentObject doc : list){
			synchronized (doc) {
				doc.resampleTopics(vMod, modulo,numOFTopics,v,topicCounters);
				
			}
		}
		
	}
    
}
