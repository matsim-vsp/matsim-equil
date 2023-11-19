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

import com.google.inject.Inject;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.contrib.otfvis.OTFVis;
import org.matsim.contrib.otfvis.OTFVisLiveModule;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.FacilitiesConfigGroup;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.IterationCounter;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.mobsim.framework.events.MobsimInitializedEvent;
import org.matsim.core.mobsim.framework.listeners.MobsimInitializedListener;
import org.matsim.core.mobsim.qsim.QSim;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.vis.otfvis.OTFClientLive;
import org.matsim.vis.otfvis.OTFVisConfigGroup;
import org.matsim.vis.otfvis.OnTheFlyServer;
import org.matsim.vis.otfvis.OnTheFlyServer.NonPlanAgentQueryHelper;

import java.util.Collections;

/**
 * @author nagel
 *
 */
public class RunMatsim {
	private final String[] args;
	private Config config ;
	private Scenario scenario ;
	private Controler controler ;

	public RunMatsim( String [] args ) {
		this.args = args ;
	}

	public final Config prepareConfig() {
		if ( args!=null && args.length>0 && args[0]!=null && args[0].length() > 0 ){
			config = ConfigUtils.loadConfig( args[0] ) ;
		} else {
			config = ConfigUtils.loadConfig( "./scenarios/equil/config.xml" ) ;
			config.controller().setOverwriteFileSetting( OverwriteFileSetting.deleteDirectoryIfExists );
		}
		config.qsim().setTrafficDynamics( QSimConfigGroup.TrafficDynamics.kinematicWaves );
		config.qsim().setSnapshotStyle( QSimConfigGroup.SnapshotStyle.kinematicWaves );

		config.qsim().setEndTime( 36.*3600. );
		config.qsim().setSimEndtimeInterpretation( QSimConfigGroup.EndtimeInterpretation.onlyUseEndtime );

		config.controller().setWriteEventsInterval( 1 );

		config.facilities().setFacilitiesSource( FacilitiesConfigGroup.FacilitiesSource.onePerActivityLinkInPlansFile );

		OTFVisConfigGroup otfConfig = ConfigUtils.addOrGetModule( config, OTFVisConfigGroup.class );
		return config ;
	}

	public final Scenario prepareScenario() {
		if ( config==null ) {
			prepareConfig() ;
		}
		scenario = ScenarioUtils.loadScenario( config ) ;
		return scenario ;
	}

	public final Controler prepareControler() {
		if ( scenario==null ) {
			this.prepareScenario() ;
		}
		controler = new Controler( scenario ) ;
		return controler ;
	}

	public final void run() {
		if ( controler==null ) {
			this.prepareControler() ;
		}
//		controler.addOverridingModule( new AbstractModule(){
//			@Override
//			public void install(){
////				this.addMobsimListenerBinding().to( OTFVisMobsimListener.class ) ;
//			}
//		} );
		controler.addOverridingModule( new OTFVisLiveModule() );
		controler.run() ;
	}

	public static void main(String[] args) {
		new RunMatsim( args ).run() ;
	}

	private static class OTFVisMobsimListener implements MobsimInitializedListener{
		@Inject Scenario scenario ;
		@Inject EventsManager events ;
		@Inject(optional=true) NonPlanAgentQueryHelper nonPlanAgentQueryHelper;
		@Inject IterationCounter itCounter ;
		@Override public void notifyMobsimInitialized( MobsimInitializedEvent e ) {
			if ( itCounter.getIterationNumber() % scenario.getConfig().controller().getWriteSnapshotsInterval() == 0 ) {
				QSim qsim = (QSim) e.getQueueSimulation();
				OnTheFlyServer server = OTFVis.startServerAndRegisterWithQSim( scenario.getConfig(), scenario, events, qsim, nonPlanAgentQueryHelper );
				OTFClientLive.run( scenario.getConfig(), server );
			}
		}
	}


}
