/* *********************************************************************** *
 * project: org.matsim.*												   *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */
package org.matsim.run;

import org.apache.logging.log4j.LogManager;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.matsim.core.config.Config;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.testcases.MatsimTestUtils;

/**
 * @author nagel
 *
 */
public class RunMatsimTest{
	
	@Rule public MatsimTestUtils utils = new MatsimTestUtils() ;

	@Test
	@Ignore
	public final void test() {
		try {
			RunMatsim runMatsim = new RunMatsim( null );;

			Config config = runMatsim.prepareConfig() ;
			config.controller().setWriteEventsInterval(1);
			config.controller().setLastIteration(1);
			config.controller().setOutputDirectory( utils.getOutputDirectory() );
			config.controller().setOverwriteFileSetting(OverwriteFileSetting.deleteDirectoryIfExists);

			runMatsim.run() ;

		} catch ( Exception ee ) {
			LogManager.getLogger(this.getClass() ).fatal("there was an exception: \n" + ee ) ;

			// if one catches an exception, then one needs to explicitly fail the test:
			Assert.fail();
		}


	}

}
