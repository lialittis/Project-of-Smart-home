package org.example.follow.me.manager.command;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Requires;
import org.example.follow.me.manager.EnergyGoal;
import org.example.follow.me.manager.FollowMeAdministration;
import org.example.follow.me.manager.IlluminanceGoal;

import fr.liglab.adele.icasa.command.handler.Command;
import fr.liglab.adele.icasa.command.handler.CommandProvider;
import fr.liglab.adele.icasa.service.preferences.Preferences;

//Define this class as an implementation of a component :
@Component
//Create an instance of the component
@Instantiate(name = "follow.me.mananger.command")
//Use the handler command and declare the command as a command provider. The
//namespace is used to prevent name collision.
@CommandProvider(namespace = "followme")
public class FollowMeManagerCommandImpl {

	// Declare a dependency to a FollowMeAdministration service
	@Requires
	private FollowMeAdministration m_administrationService;
	
	@Requires
	private Preferences PreferencesService;

	/**
	 * Felix shell command implementation to sets the illuminance preference.
	 *
	 * @param goal the new illuminance preference ("SOFT", "MEDIUM", "FULL")
	 */

	// Each command should start with a @Command annotation
	@Command
	public synchronized void setIlluminancePreference(String goal) {
		// The targeted goal
		IlluminanceGoal illuminanceGoal;

		// TODO : Here you have to convert the goal string into an illuminance
		// goal and fail if the entry is not "SOFT", "MEDIUM" or "HIGH"
		switch (goal) {
		case "SOFT":
			illuminanceGoal = IlluminanceGoal.SOFT;
			break;
		case "MEDIUM":
			illuminanceGoal = IlluminanceGoal.MEDIUM;
			break;
		case "FULL":
			illuminanceGoal = IlluminanceGoal.FULL;
			break;
		default:
			throw new IllegalArgumentException("Invalid illuminance goal !" + goal);

		}
		//call the administration service to configure it :
		m_administrationService.setIlluminancePreference(illuminanceGoal);
	}

	@Command
	public synchronized void getIlluminancePreference() {
		//TODO : implement the command that print the current value of the goal
		System.out.println("The illuminance goal is " + m_administrationService.getIlluminancePreference());
	}
	
	@Command
	public synchronized void setEnergyPreference(String goal) {
		
		// The targeted goal
		EnergyGoal energyGoal;

		switch (goal) {
		case "LOW":
			energyGoal = EnergyGoal.LOW;
			break;
		case "MEDIUM":
			energyGoal = EnergyGoal.MEDIUM;
			break;
		case "HIGH":
			energyGoal = EnergyGoal.HIGH;
			break;
		default:
			throw new IllegalArgumentException("Invalid energy goal !" + goal);
		}
		m_administrationService.setEnergySavingGoal(energyGoal);
	}

	@Command
	public synchronized void getEnergyPreference() {
		// TODO Auto-generated method stub
		System.out.println("EnergyMode = " + m_administrationService.getEnergyGoal());
	}
	
	@Command
	public synchronized void setUserPreference(String name,String goal) {
		// The targeted goal
		IlluminanceGoal illuminanceGoal;

		// goal and fail if the entry is not "SOFT", "MEDIUM" or "HIGH"
		switch (goal) {
		case "SOFT":
			illuminanceGoal = IlluminanceGoal.SOFT;
			break;
		case "MEDIUM":
			illuminanceGoal = IlluminanceGoal.MEDIUM;
			break;
		case "FULL":
			illuminanceGoal = IlluminanceGoal.FULL;
			break;
		default:
			throw new IllegalArgumentException("Invalid illuminance goal !" + goal);
		}
		//call the preferences service to configure it :
		m_administrationService.setUserPreference(name,illuminanceGoal);
	}

	@Command
	public synchronized void getUserPreference(String name) {
		//TODO : implement the command that print the current value of the goal
		System.out.println("The illuminance goal for the user "+ name + " is " + m_administrationService.getUserPreference(name)); 
	}

}
