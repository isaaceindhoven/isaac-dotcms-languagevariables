package nl.isaac.dotcms.languagevariables.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.dotmarketing.util.Logger;
/**
 * <p>
 * A utility class that can be used to time the execution time of code. It is designed as a singleton
 * so it can be used throughout the code without the need to pass it along.
 * </p>
 * 
 * <p>
 * It provides a grouping mechanism that can contain up to 100 separate timers each.
 * Each group is identified by a string, each timer within the group is identified by 
 * an int (0-99). 
 * </p>
 * 
 * <p>
 * By calling <code>start(group, id)</code> you start the timer with the given id within the
 * given group. By calling <code>stop(group, id)</code> you stop the timer. For example
 * </p>
 * 
 * <code>
 * <pre>
 * TimingSingleton ts = TimingSingleton.getInstance();
 * ts.resetAll("group1");
 * ts.start("group1", 0);
 * ... random code...
 * ts.stop("group1", 0);
 *
 * ts.start("group1", 1);
 * ... other random code...
 * ts.stop("group1", 1);
 * </pre>
 * </code>
 * 
 * <p>
 * The code above first clears all the timers within the group (not mandatory) and times the time it 
 * takes for the random code to execute. You can see how long it took  by calling <code>ts.getTime("group1", 0);
 * </code> and <code>ts.getTime("group1", 1);</code> separately.  
 * </p>
 * 
 * <p>
 * You can also see how long the timers within the group took compared to each other by calling
 * the <code>toString(group)</code> method. It will return some statistics about the group including
 * the absolute and relative time all timers within the group took, how often each timer was started 
 * and stopped and some more info. Like this:
 * </p>
 * 
 * <p>
 * <pre>
 * total time in group 'group1': 32.0
 * id  msec  msec%  calls  avg   max  min  relative weight of one call
 * 0   32    100%   1      32.0  32   32   100
 * 1   0     0%     1      0.0   0    0    0
 * </pre>
 * </p>
 * 
 * @author Koen Peters, Cubix Concepts
 */
public final class TimingSingleton {
	
	// Private constructor prevents instantiation from other classes
	private TimingSingleton() {	}
	
	/**
	* SingletonHolder is loaded on the first execution of Singleton.getInstance() 
	* or the first access to SingletonHolder.INSTANCE, not before.
	*/
	private static class SingletonHolder { 
	  public static final TimingSingleton instance = new TimingSingleton();
	}
 
	public static TimingSingleton getInstance() {
	  return SingletonHolder.instance;
	}
	
	// The map of all groups
	private Map<String, TimingGroup> containers = new HashMap<String, TimingGroup>();
	
	public void resetAll(String group) {
		getGroup(group).resetAll();
	}

	public void reset(String group, int id) {
		getGroup(group).reset(id);
	}
	
	public void start(String group, int id) {
		getGroup(group).start(id);
	}
	
	public void stop(String group, int id) {
		getGroup(group).stop(id);
	}

	public Long getTime(String group, int id) {
		return getGroup(group).getTime(id);
	}

	public String toString(String group) {
		return getGroup(group).toString();
	}
	
	public void resetAll(Object object) {
		resetAll(object.getClass().toString());
	}
	
	public void reset(Object object, int id) {
		reset(object.getClass().toString(), id);
	}
	
	public void start(Object object, int id) {
		start(object.getClass().toString(), id);
	}
	
	public void stop(Object object, int id) {
		stop(object.getClass().toString(), id);
	}
	
	public Long getTime(Object object, int id) {
		return getTime(object.getClass().toString(), id);
	}
	
	public String toString(Object object) {
		return toString(object.getClass().toString());
	}
	
	private TimingGroup getGroup(String group) {
		TimingGroup result = containers.get(group);
		if (result == null) {
			result = new TimingGroup(group);
			containers.put(group, result);
		}
		return result;
	}
	
	private class TimingGroup {
		long[] times = new long[100];
		long[] running = new long[100];
		long[] nrOfMeasurement = new long[100];
		long[] max = new long[100];
		long[] min = new long[100];
		String group;
		
		TimingGroup(String group) {
			this.group = group;
			resetAll();
		}
		
		void resetAll() {
			Arrays.fill(times, 0);
			Arrays.fill(running, 0);
			Arrays.fill(nrOfMeasurement, 0);
			Arrays.fill(max, 0);
			Arrays.fill(min, Integer.MAX_VALUE);
		}
		void reset(int id) {
			times[id] = 0;
			running[id] = 0;
			nrOfMeasurement[id] = 0;
			max[id] = 0;
			min[id] = Integer.MAX_VALUE;
		}
		
		void start(int id) {
			running[id] = System.currentTimeMillis(); 
		}
		
		void stop(int id) {
			long endTime = System.currentTimeMillis();
			if (running[id] == 0) {
				Logger.error(TimingSingleton.class,"called stop before calling start with id " + id);
			} else {
				long diff = endTime - running[id];
				times[id] = times[id] + diff;
				nrOfMeasurement[id] = nrOfMeasurement[id] + 1;
				running[id] = 0;
				max[id] = Math.max(max[id], diff);
				min[id] = Math.min(min[id], diff);
			}
		}
		
		long getTime(int id) {
			return times[id];
		}

		
		long getTotalTime() {
			long result = 0;
			for (int i = 0; i < times.length; i++) {
				result += times[i];
			}
			return result;
		}
		
		long getTotalMeasurements() {
			long result = 0;
			for (int i = 0; i < nrOfMeasurement.length; i++) {
				result += nrOfMeasurement[i];
			}
			return result;
		}
		
		int getNumberOfChannels() {
			int result = 0;
			for (int i=0; i < times.length; i++) {
				if (nrOfMeasurement[i] > 0) {
					result++;
				}
			}
			return result;
		}
		
		public String toString() {
			float totalTime = getTotalTime();
			float totalMeasurements = new Float(getTotalMeasurements());
			float nrOfChanels = new Float(getNumberOfChannels());
			
			StringBuffer result = new StringBuffer();
			result
				.append("\ntotal time in group '" ).append(group).append("': ").append(totalTime).append("\n")
				.append("id")
				.append("\tmsec")
				.append("\tmsec%")
				.append("\tcalls") 
				.append("\tavg")
				.append("\tmax")
				.append("\tmin")
				.append("\trelative weight of one call")
				.append("\n");
			
			for (int i=0; i < times.length; i++) {
				if (nrOfMeasurement[i] > 0) {
					float relativeFactor = (1f / nrOfChanels) / (new Float(nrOfMeasurement[i]) / totalMeasurements);
					float percentage = (new Float(times[i]) / new Float(totalTime)) * 100;
					float avg = (new Float(times[i]) / new Float(nrOfMeasurement[i]));
					result	.append(i)
							.append("\t").append(times[i]) 
							.append("\t").append(Math.round(percentage)).append("%")
							.append("\t").append(nrOfMeasurement[i]) 
							.append("\t").append(Math.round(avg))
							.append("\t").append(Math.round(max[i]))
							.append("\t").append(Math.round(min[i]))
							.append("\t").append(Math.round(relativeFactor * percentage))
							.append("\n");
				}
			}
			
			return result.toString();
		}

		
	}
}
