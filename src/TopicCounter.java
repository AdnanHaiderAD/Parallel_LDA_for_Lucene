package parallelLDA;
/** @author adnan
 * Each topic counter object represents an abstract topic in the corpus i.e a particular distribution of words
 * @author adnan
 *
 */
public class TopicCounter {

	private int total;
	private int[] wordCounts;
	
	
	public TopicCounter(int numOfDistinctWords){
		this.wordCounts = new int[numOfDistinctWords];
		
	}
	
	public void incrementwWordCount(int wordId, int count){
		wordCounts[wordId]+=count;
		total+=count;
	}
	
	public int getWordFreq(int wordId){
		return wordCounts[wordId];
	}
	
	public int getTotalWords(){
		return total;
	}
	
	public int[] getWordDist(){
		return wordCounts;
	}
	
	public void syncCounts(TopicCounter localThreadCounter, boolean sync, int threadID, int numOfcpus){   
		if (sync){
			for (int i = threadID ; i <wordCounts.length ;i+=numOfcpus){
				int count = localThreadCounter.wordCounts[i];
				if (count!=0 ){
					wordCounts[i]+=count;
					localThreadCounter.wordCounts[i] =0;
				}
			}
			
		}else{
			synchronized (this) {
			total+= localThreadCounter.getTotalWords();
			localThreadCounter.total = 0;
			}
		}	
	
			
		}
}
