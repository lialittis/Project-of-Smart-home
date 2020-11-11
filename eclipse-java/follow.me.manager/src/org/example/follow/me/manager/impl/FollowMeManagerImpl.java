package org.example.follow.me.manager.impl;

import org.example.follow.me.manager.EnergyGoal;
import org.example.follow.me.manager.FollowMeAdministration;
import org.example.follow.me.manager.IlluminanceGoal;
import org.example.follow.me.configuration.FollowMeConfiguration;
import fr.liglab.adele.icasa.service.location.PersonLocationService;
import fr.liglab.adele.icasa.service.preferences.Preferences;

public class FollowMeManagerImpl implements FollowMeAdministration {

	/** Field for FollowMeConfiguration dependency */
	private FollowMeConfiguration FollowMeConfiguration;

	/** Field for Location.Service dependency */
	private PersonLocationService LocationService;
	/** Field for Service.Preferences dependency */
	private Preferences PreferencesService;

	/**
	* User preferences for illuminance
	**/
	public static final String USER_PROP_ILLUMINANCE = "illuminance";
	public static final String USER_PROP_ILLUMINANCE_VALUE_SOFT = "SOFT";
	public static final String USER_PROP_ILLUMINANCE_VALUE_MEDIUM = "MEDIUM";
	public static final String USER_PROP_ILLUMINANCE_VALUE_FULL = "FULL";

	@Override
	public synchronized void setIlluminancePreference(IlluminanceGoal illuminanceGoal) {
		// TODO Auto-generated method stub
		int maximumNumberOfLightsToTurnOn = illuminanceGoal.getNumberOfLightsToTurnOn();
		double targetedIlluminance = illuminanceGoal.getTargetedIlluminance();
		FollowMeConfiguration.setMaximumNumberOfLightsToTurnOn(maximumNumberOfLightsToTurnOn);
		FollowMeConfiguration.setTargetedIlluminance(targetedIlluminance);
	}

	@Override
	public synchronized IlluminanceGoal getIlluminancePreference() {
		// TODO Auto-generated method stub
		int maximumNumberOfLightsToTurnOn = FollowMeConfiguration.getMaximumNumberOfLightsToTurnOn();
		double targetedIlluminance = FollowMeConfiguration.getTargetedIlluminance();

		// The targeted goal
		IlluminanceGoal illuminanceGoal;

		switch (maximumNumberOfLightsToTurnOn) {
		case 1:
			illuminanceGoal = IlluminanceGoal.SOFT;
			break;
		case 2:
			illuminanceGoal = IlluminanceGoal.MEDIUM;
			break;
		case 3:
			illuminanceGoal = IlluminanceGoal.FULL;
			break;
		default:
			throw new IllegalArgumentException("Invalid illuminance goal !" + maximumNumberOfLightsToTurnOn);
		}

		return illuminanceGoal;
	}

	/** Component Lifecycle Method */
	public void stop() {
		// TODO: Add your implementation code here
		System.out.println("The Manager is stopping...");
	}

	/** Component Lifecycle Method */
	public void start() {
		// TODO: Add your implementation code here
		System.out.println("The Manager is starting...");
	}

	@Override
	public synchronized void setEnergySavingGoal(EnergyGoal energyGoal) {
		double maximumEnergyConsumptionAllowedInARoom = energyGoal.getMaximumEnergyInRoom();
		FollowMeConfiguration.setMaximumAllowedEnergyInRoom(maximumEnergyConsumptionAllowedInARoom);
	}

	@Override
	public synchronized EnergyGoal getEnergyGoal() {
		// TODO Auto-generated method stub
		double maximumEnergyConsumptionAllowedInARoom = FollowMeConfiguration.getMaximumAllowedEnergyInRoom();

		// The targeted goal
		EnergyGoal energyGoal;

		int target = (int) (maximumEnergyConsumptionAllowedInARoom / 100);

		switch (target) {
		case 1:
			energyGoal = EnergyGoal.LOW;
			break;
		case 2:
			energyGoal = EnergyGoal.MEDIUM;
			break;
		case 10:
			energyGoal = EnergyGoal.HIGH;
			break;
		default:
			throw new IllegalArgumentException("Invalid energy goal !" + maximumEnergyConsumptionAllowedInARoom);
		}

		return energyGoal;
	}

	public synchronized IlluminanceGoal getUserPreference(String name) {
		
		
		String UserIllum = (String) PreferencesService.getUserPropertyValue(name, USER_PROP_ILLUMINANCE);
		// The targeted goal
		IlluminanceGoal illuminanceGoal;

		switch (UserIllum) {
		case USER_PROP_ILLUMINANCE_VALUE_SOFT:
			illuminanceGoal = IlluminanceGoal.SOFT;
			break;
		case USER_PROP_ILLUMINANCE_VALUE_MEDIUM:
			illuminanceGoal = IlluminanceGoal.MEDIUM;
			break;
		case USER_PROP_ILLUMINANCE_VALUE_FULL:
			illuminanceGoal = IlluminanceGoal.FULL;
			break;
		default:
			throw new IllegalArgumentException("Invalid illuminance goal !" + UserIllum);
		}

		return illuminanceGoal;
	}

	public synchronized void setUserPreference(String name, IlluminanceGoal illuminanceGoal) {
		PreferencesService.setUserPropertyValue(name, USER_PROP_ILLUMINANCE, illuminanceGoal.toString());
	}
	
	
	

}
