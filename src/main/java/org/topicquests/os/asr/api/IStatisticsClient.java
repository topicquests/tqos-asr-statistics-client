/**
 * 
 */
package org.topicquests.os.asr.api;

import org.topicquests.support.api.IResult;

/**
 * @author jackpark
 *
 */
public interface IStatisticsClient {

	IResult addToKey(String key);
	
	IResult getStatistics();
	
	IResult getValueOfKey(String key);
}
