/*-
 * #%L
 * TrackMate: your buddy for everyday tracking.
 * %%
 * Copyright (C) 2021 - 2023 TrackMate developers.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package fiji.plugin.trackmate.cellpose.tbonte;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CellposeSettings
{

	public final String cellposePythonPath;
	
	public final int chan;

	public final int chan2;

	public final PretrainedModel model;

	public final String customModelPath;

	public final double diameter;

	public final boolean useGPU;

	public final boolean simplifyContours;
	
	public final double flowThreshold;
	
	public final double cellprobThreshold;
	
	public final boolean augment;


	public CellposeSettings(
			final String cellposePythonPath,
			final PretrainedModel model,
			final String customModelPath,
			final int chan,
			final int chan2,
			final double diameter,
			final boolean useGPU,
			final boolean simplifyContours,
			final double flowThreshold,
			final double cellprobThreshold,
			final boolean augment )
	{
		this.cellposePythonPath = cellposePythonPath;
		this.model = model;
		this.customModelPath = customModelPath;
		this.chan = chan;
		this.chan2 = chan2;
		this.diameter = diameter;
		this.useGPU = useGPU;
		this.simplifyContours = simplifyContours;
		this.flowThreshold = flowThreshold;
		this.cellprobThreshold = cellprobThreshold;
		this.augment = augment;
	}

	public List< String > toCmdLine( final String imagesDir )
	{
		final List< String > cmd = new ArrayList<>();

		/*
		 * First decide whether we are calling Cellpose from python, or directly
		 * the Cellpose executable. We check the last part of the path to check
		 * whether this is python or cellpose.
		 */
		final String[] split = cellposePythonPath.replace( "\\", "/" ).split( "/" );
		final String lastItem = split[ split.length - 1 ];
		if ( lastItem.toLowerCase().startsWith( "python" ) )
		{
			// Calling Cellpose from python.
			cmd.add( cellposePythonPath );
			cmd.add( "-m" );
			// Call custom cellpose version (where augment can be enabled)
			cmd.add( "custom-cellpose" );
		}
		else
		{
			// Calling Cellpose executable.
			cmd.add( cellposePythonPath );
		}

		/*
		 * Cellpose command line arguments.
		 */

		// Target dir.
		cmd.add( "--dir" );
		cmd.add( imagesDir );

		// First channel.
		cmd.add( "--chan" );
		cmd.add( "" + chan );

		// Second channel.
		if ( chan2 >= 0 )
		{
			cmd.add( "--chan2" );
			cmd.add( "" + chan2 );
		}

		// GPU.
		if ( useGPU )
			cmd.add( "--use_gpu" );

		// Diameter.
		cmd.add( "--diameter" );
		cmd.add( ( diameter > 0 ) ? "" + diameter : "0" );
		
		// Flow threshold.
		cmd.add( "--flow_threshold" );
		cmd.add( "" + flowThreshold );
		
		// Flow threshold.
		cmd.add( "--cellprob_threshold" );
		cmd.add( "" + cellprobThreshold );
		
		// Augment.
		if ( augment )
			cmd.add( "--augment" );

		// Model.
		cmd.add( "--pretrained_model" );
		if ( model == PretrainedModel.CUSTOM )
			cmd.add( customModelPath );
		else
			cmd.add( model.path );

		// Export results as PNG.
		cmd.add( "--save_png" );

		// Do not save Numpy files.
		cmd.add( "--no_npy" );
		
		// Enable fast mode.
		cmd.add( "--fast_mode" );

		return Collections.unmodifiableList( cmd );
	}

	public static Builder create()
	{
		return new Builder();
	}

	public static final class Builder
	{

		private String cellposePythonPath = "/opt/anaconda3/envs/cellpose/bin/python";

		private int chan = 0;

		private int chan2 = -1;

		private PretrainedModel model = PretrainedModel.CYTO;

		private double diameter = 30.;
		
		private boolean useGPU = true;
		
		private boolean simplifyContours = true;

		private String customModelPath = "";
		
		private double flowThreshold = 0.4;
		
		private double cellprobThreshold = 0.;
		
		private boolean augment = false;

		public Builder channel1( final int ch )
		{
			this.chan = ch;
			return this;
		}

		public Builder channel2( final int ch )
		{
			this.chan2 = ch;
			return this;
		}

		public Builder cellposePythonPath( final String cellposePythonPath )
		{
			this.cellposePythonPath = cellposePythonPath;
			return this;
		}

		public Builder model( final PretrainedModel model )
		{
			this.model = model;
			return this;
		}

		public Builder diameter( final double diameter )
		{
			this.diameter = diameter;
			return this;
		}

		public Builder useGPU( final boolean useGPU )
		{
			this.useGPU = useGPU;
			return this;
		}

		public Builder simplifyContours( final boolean simplifyContours )
		{
			this.simplifyContours = simplifyContours;
			return this;
		}
		
		public Builder flowThreshold( final double flowThreshold )
		{
			this.flowThreshold = flowThreshold;
			return this;
		}
		
		public Builder cellprobThreshold( final double cellprobThreshold )
		{
			this.cellprobThreshold = cellprobThreshold;
			return this;
		}
		
		public Builder augment( final boolean augment )
		{
			this.augment = augment;
			return this;
		}

		public Builder customModel( final String customModelPath )
		{
			this.customModelPath = customModelPath;
			return this;
		}

		public CellposeSettings get()
		{
			return new CellposeSettings(
					cellposePythonPath,
					model,
					customModelPath,
					chan,
					chan2,
					diameter,
					useGPU,
					simplifyContours,
					flowThreshold,
					cellprobThreshold,
					augment);
		}

	}

	public enum PretrainedModel
	{
		CYTO( "Cytoplasm", "cyto" ),
		NUCLEI( "Nucleus", "nuclei" ),
		CYTO2( "Cytoplasm 2.0", "cyto2" ),
		CUSTOM( "Custom", "" );

		private final String name;

		private final String path;

		PretrainedModel( final String name, final String path )
		{
			this.name = name;
			this.path = path;
		}

		@Override
		public String toString()
		{
			return name;
		}

		public String cellposeName()
		{
			return path;
		}
	}

	public static final CellposeSettings DEFAULT = new Builder().get();

}
