/**
 *	@file LargeStarDriver.java
 *	@brief Driver of the Job responsible for executing the Small-Star or Large-Star operation on the input graph.
 *  @author Federico Conte (draxent)
 *  
 *	Copyright 2015 Federico Conte
 *	https://github.com/Draxent/ConnectedComponents
 * 
 *	Licensed under the Apache License, Version 2.0 (the "License"); 
 *	you may not use this file except in compliance with the License. 
 *	You may obtain a copy of the License at 
 * 
 *	http://www.apache.org/licenses/LICENSE-2.0 
 *  
 *	Unless required by applicable law or agreed to in writing, software 
 *	distributed under the License is distributed on an "AS IS" BASIS, 
 *	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *	See the License for the specific language governing permissions and 
 *	limitations under the License. 
 */

package pad;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.Tool;

/**	Driver of the Job responsible for executing the Small-Star or Large-Star operation on the input graph. */
public class StarDriver extends Configured implements Tool
{
	public enum StarDriverType { LARGE, SMALL };
	private final String title;
	private final StarDriverType type;
	private final String graphInput;
	private final String graphOutput;
	private final boolean verbose;
	
	/**
	* Initializes a new instance of the StarDriver class.
	* @param type		identify which kind of job execute: Small-Star or Large-Star.
	* @param graphInput	piece of the path of the result folder of \see InitializationDriver or \see StarDriver Job.
	* @param iteration	identify the input path and output path used by this Job.
	* @param verbose	if <c>true</c> shows on screen the messages of the Job execution.
	*/
	public StarDriver( StarDriverType type, String graphInput, int iteration, boolean verbose )
	{
		this.type = type;
		this.title = type.equals( StarDriverType.SMALL ) ? "Small-Star" + iteration : "Large-Star" + iteration;
		this.graphInput = graphInput + "_" + iteration;
		this.graphOutput = graphInput + "_" + ( iteration + 1 );
		this.verbose = verbose;
	}
	
	/**
	 * Execute the StarDriver Job.
	 * @param args		array of external arguments, not used in this method
	 * @return 			<c>1</c> if the StarDriver Job failed its execution; <c>0</c> if everything is ok. 
	 * @throws Exception 
	 */
	public int run( String[] args ) throws Exception
	{
		Configuration conf = new Configuration();
		// GenericOptionsParser invocation in order to suppress the hadoop warning.
		new GenericOptionsParser( conf, args );
		conf.set( "type", this.type.toString() );
		Job job = new Job( conf, this.title );
		job.setJarByClass( StarDriver.class );
	
		job.setMapOutputKeyClass( NodesPair.class );
		job.setMapOutputValueClass( IntWritable.class );
		job.setOutputKeyClass( IntWritable.class );
		job.setOutputValueClass( IntWritable.class );
	
		job.setMapperClass( StarMapper.class );
		job.setPartitionerClass( NodePartitioner.class );
		job.setGroupingComparatorClass( NodeGroupingComparator.class );
		job.setReducerClass( StarReducer.class );
	
		job.setInputFormatClass( TextInputFormat.class );
		job.setOutputFormatClass( TextOutputFormat.class );
	
		FileInputFormat.addInputPath( job, new Path ( graphInput ) );
		FileOutputFormat.setOutputPath( job, new Path ( graphOutput ) );

		return job.waitForCompletion( verbose ) ? 0 : 1;
	}
}
