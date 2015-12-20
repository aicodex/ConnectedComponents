/**
 *	@file LargeStartMapper.java
 *	@brief Mapper task of the \see StarDriver Job.
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

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Mapper;

/** Mapper task of the \see StarDriver Job. */
public class StarMapper extends Mapper<IntWritable, IntWritable, NodesPairWritable, IntWritable> 
{
	private boolean smallStar;
	private NodesPairWritable pair = new NodesPairWritable();

	/**
	* Setup method of the this StarMapper class.
	* Extract the <em>type</em> variable from the context configuration.
	* Based on this value, this Mapper will behave as a Small-Star Mapper or Large-Star Mapper.
	* @param context	context of this Job.
	*/
	public void setup( Context context )
	{
		smallStar = context.getConfiguration().get( "type" ).equals( "SMALL" );
	}
	
	/**
	* Map method of the this StarMapper class.
	* If it is a Large-Star Mapper, it emits the pairs <u,v> and <v,u>.
	* If it is a Small-Star Mapper, it emits the pair <max(u,v), min(u,v)>.
	* @param nodeID			identifier of the node.
	* @param neighborID		identifier of the neighbor.
	* @param context	context of this Job.
	* @throws IOException, InterruptedException
	*/
	public void map( IntWritable nodeID, IntWritable neighborID, Context context ) throws IOException, InterruptedException 
	{		
		// if the node is alone, emit it like is it in order to keep that information
		if ( neighborID.get() == -1 )
		{
			// Set up the pair.
			pair.NodeID = nodeID.get();
			pair.NeighborID =  neighborID.get();
			
			context.write( pair, neighborID );
			return;
		}
		
		// If we are running Small-Star, we emit only when the neighborID is smaller than nodeID
		if ( smallStar )
		{
			// if the label of neighbor is less than the label of the node
			if ( neighborID.get() < nodeID.get() )
			{
				// Set up the pair.
				pair.NodeID = nodeID.get();
				pair.NeighborID =  neighborID.get();
				
				context.write( pair, neighborID );
			}
			else
			{
				// Set up the pair.
				pair.NodeID = neighborID.get();
				pair.NeighborID =  nodeID.get();
				
				context.write( pair, nodeID );
			}
		}
		// If we are running Large-Star, we always emit: <NodeID; NeighborID> and <NeighborID; NodeID>
		else
		{
			// Set up the pair.
			pair.NodeID = nodeID.get();
			pair.NeighborID =  neighborID.get();
			
			// Emit <NodeID; NeighborID>
			context.write( pair, neighborID );
			
			// Set up the pair.
			pair.NodeID = neighborID.get();
			pair.NeighborID =  nodeID.get();
			
			// Emit <NeighborID; NodeID>
			context.write( pair, nodeID );
		}
	}
}