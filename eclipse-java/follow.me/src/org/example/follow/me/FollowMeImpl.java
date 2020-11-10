package org.example.follow.me;

import fr.liglab.adele.icasa.device.presence.PresenceSensor;
import fr.liglab.adele.icasa.device.DeviceListener;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.light.BinaryLight;
import org.example.follow.me.configuration.FollowMeConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import fr.liglab.adele.icasa.device.light.DimmerLight;

public class FollowMeImpl implements DeviceListener, FollowMeConfiguration {

	/** Field for presenceSensors dependency */
	private PresenceSensor[] presenceSensors;
	/** Field for binaryLights dependency */
	private BinaryLight[] binaryLights;

	/** Field for dimmerLights dependency */
	private DimmerLight[] dimmerLights;

	/**
	 * The name of the LOCATION property
	 */
	public static final String LOCATION_PROPERTY_NAME = "Location";

	/**
	 * The name of the location for unknown value
	 */
	public static final String LOCATION_UNKNOWN = "unknown";

	/** 
	* The maximum number of lights to turn on when a user enters the room :
	**/
	private int maxLightsToTurnOnPerRoom = 1;

	/**
	* The maximum energy consumption allowed in a room in Watt:
	**/
	private double maximumEnergyConsumptionAllowedInARoom = 100.0d;

	/**
	* The targeted illuminance in each room
	**/
	private double targetedIlluminance = 4000.0d;

	/**
	 * Watt to lumens conversion factor
	 * It has been considered that: 1 Watt=680.0 lumens at 555nm.
	 */
	public final static double ONE_WATT_TO_ONE_LUMEN = 680.0d;

	/** 
	 * Bind Method for presenceSensors dependency 
	 * This method will be used to manage device listener
	 * */
	public synchronized void bindPresenceSensor(PresenceSensor presenceSensor, Map properties) {
		presenceSensor.addListener(this);
		System.out.println("bind presence sensor " + presenceSensor.getSerialNumber());
	}

	/** Unbind Method for presenceSensors dependency */
	public synchronized void unbindPresenceSensor(PresenceSensor presenceSensor, Map properties) {
		presenceSensor.removeListener(this);
		System.out.println("unbind presence sensor " + presenceSensor.getSerialNumber());
	}

	/** Bind Method for binaryLights dependency */
	public synchronized void bindBinaryLight(BinaryLight binaryLight, Map properties) {
		binaryLight.addListener(this);
		System.out.println("bind binary light " + binaryLight.getSerialNumber());
	}

	/** 
	 * Unbind Method for binaryLights dependency 
	 * This method is not mandatory and implemented for debug purpose only.
	 * */
	public synchronized void unbindBinaryLight(BinaryLight binaryLight, Map properties) {
		binaryLight.removeListener(this);
		System.out.println("unbind binary light " + binaryLight.getSerialNumber());
	}

	/** Bind Method for dimmerLights dependency */
	public void bindDimmerLight(DimmerLight dimmerLight, Map properties) {
		dimmerLight.addListener(this);
		System.out.println("bind dimmer light " + dimmerLight.getSerialNumber());
	}

	/** Unbind Method for dimmerLights dependency */
	public void unbindDimmerLight(DimmerLight dimmerLight, Map properties) {
		dimmerLight.removeListener(this);
		System.out.println("unbind dimmer light " + dimmerLight.getSerialNumber());
	}

	/** Component Lifecycle Method */
	public synchronized void stop() {
		for (PresenceSensor sensor : presenceSensors) {
			sensor.removeListener(this);
		}
		for (BinaryLight light : binaryLights) {
			light.removeListener(this);
		}
		for (DimmerLight dlight : dimmerLights) {
			dlight.removeListener(this);
		}
		System.out.println("Component is stopping...");
	}

	/** Component Lifecycle Method */
	public synchronized void start() {
		System.out.println("Component is starting...");
	}

	/**
	 * Return all BinaryLight from the given location
	 * 
	 * @param location
	 *            : the given location
	 * @return the list of matching BinaryLights
	 */

	private synchronized List<BinaryLight> getBinaryLightsFromLocation(String location) {
		List<BinaryLight> binaryLightsLocation = new ArrayList<BinaryLight>();
		for (BinaryLight binLight : binaryLights) {
			if (binLight.getPropertyValue(LOCATION_PROPERTY_NAME).equals(location)) {
				binaryLightsLocation.add(binLight);
			}
		}
		return binaryLightsLocation;
	}

	private synchronized PresenceSensor getPresenceSensorsFromLocation(String location) {
		PresenceSensor presenceSensorLocation = null;
		for (PresenceSensor presenceSensor : presenceSensors) {
			if (presenceSensor.getPropertyValue(LOCATION_PROPERTY_NAME).equals(location)) {
				return presenceSensor;
			}
		}
		return presenceSensorLocation;
	}

	private synchronized List<DimmerLight> getDimmerLightsFromLocation(String location) {
		List<DimmerLight> dimmerLightsLocation = new ArrayList<DimmerLight>();
		for (DimmerLight dimLight : dimmerLights) {
			if (dimLight.getPropertyValue(LOCATION_PROPERTY_NAME).equals(location)) {
				dimmerLightsLocation.add(dimLight);
			}
		}
		return dimmerLightsLocation;
	}

	/**
	 * This method is part of the DeviceListener interface and is called when a
	 * subscribed device property is modified.
	 * 
	 * @param device
	 *            is the device whose property has been modified.
	 * @param propertyName
	 *            is the name of the modified property.
	 * @param oldValue
	 *            is the old value of the property
	 * @param newValue
	 *            is the new value of the property
	 */

	public void devicePropertyModified(GenericDevice device, String propertyName, Object oldValue, Object newValue) {

		if (device instanceof PresenceSensor) {
			PresenceSensor changingSensor = (PresenceSensor) device;

			// check the change is related to presence sensing
			if (propertyName.equals(PresenceSensor.PRESENCE_SENSOR_SENSED_PRESENCE)) {
				// get the location where the sensor is:
				String detectorLocation = (String) changingSensor.getPropertyValue(LOCATION_PROPERTY_NAME);

				// initialize the number of the lights

				// if the location is known :
				if (!detectorLocation.equals(LOCATION_UNKNOWN)) {

					checkingAndChangingLightsStates(changingSensor, detectorLocation);

					System.out.println("----------------------------print part------------------------------");
					System.out.println("The device with the serial number" + changingSensor.getSerialNumber()
							+ " has changed" + propertyName);
					System.out.println("This sensor is in the room :" + detectorLocation);
					System.out.println("----------------------------end print part---------------------------");
				}
			}

		} else if (device instanceof BinaryLight) {

			String oldLocation = oldValue.toString();
			String newLocation = newValue.toString();
			BinaryLight changingLight = (BinaryLight) device;

			if (propertyName.equals(LOCATION_PROPERTY_NAME)) {
				// get the location where the binary light is:
				String detectorLocation = (String) changingLight.getPropertyValue(LOCATION_PROPERTY_NAME);

				if (!detectorLocation.equals(LOCATION_UNKNOWN)) {
					// get the states of presence sensors of two locations 
					PresenceSensor oldPresenceSensor = getPresenceSensorsFromLocation(oldLocation);
					PresenceSensor newPresenceSensor = getPresenceSensorsFromLocation(newLocation);

					if (oldPresenceSensor != null) {
						checkingAndChangingLightsStates(oldPresenceSensor, oldLocation);
					}

					if (newPresenceSensor == null) {
						changingLight.turnOff();
					} else {
						changingLight.turnOff();
						checkingAndChangingLightsStates(newPresenceSensor, newLocation);
					}
					/*
					if (newPresenceSensor.getSensedPresence()) {
						int numberOfLightsTurnOnAtLocation = getNumberOfLightsTurnOn(newLocation);
						if (numberOfLightsTurnOnAtLocation < maxLightsToTurnOnPerRoom) {
							changingLight.turnOn();
						} else {
							changingLight.turnOff();
						}
					} else {
						changingLight.turnOff();
					}
					*/
					System.out.println("----------------------------print part------------------------------");
					System.out.println("The device with the serial number" + changingLight.getSerialNumber()
							+ " has changed" + propertyName);
					System.out.println("This light is in the room :" + detectorLocation);
					System.out.println("----------------------------end print part---------------------------");

				}
			}
		} else if (device instanceof DimmerLight) {
			DimmerLight changingLight = (DimmerLight) device;
			String oldLocation = oldValue.toString();
			String newLocation = newValue.toString();
			if (propertyName.equals(LOCATION_PROPERTY_NAME)) {

				// get the location where the dimmer light is:
				String detectorLocation = (String) changingLight.getPropertyValue(LOCATION_PROPERTY_NAME);
				// if the location is known :

				if (!detectorLocation.equals(LOCATION_UNKNOWN)) {
					// get the states of presence sensors of two locations 
					PresenceSensor oldPresenceSensor = getPresenceSensorsFromLocation(oldLocation);
					PresenceSensor newPresenceSensor = getPresenceSensorsFromLocation(newLocation);

					if (oldPresenceSensor != null) {
						checkingAndChangingLightsStates(oldPresenceSensor, oldLocation);
					}

					if (newPresenceSensor == null) {
						changingLight.setPowerLevel(0.0);
					} else {
						changingLight.setPowerLevel(0.0);
						checkingAndChangingLightsStates(newPresenceSensor, newLocation);
					}

					/*
					if (newPresenceSensor.getSensedPresence()) {
						int numberOfLightsTurnOnAtLocation = getNumberOfLightsTurnOn(newLocation);
						if (numberOfLightsTurnOnAtLocation < maxLightsToTurnOnPerRoom) {
							changingLight.setPowerLevel(1.0);
						} else {
							changingLight.setPowerLevel(0.0);
						}
					} else {
						changingLight.setPowerLevel(0.0);
					}
					*/
					System.out.println("----------------------------print part------------------------------");
					System.out.println("The device with the serial number" + changingLight.getSerialNumber()
							+ " has changed" + propertyName);
					System.out.println("This light is in the room :" + detectorLocation);
					System.out.println("----------------------------end print part---------------------------");
				}
			}
		}
	}

	private synchronized void checkingAndChangingLightsStates(PresenceSensor presenceSensor, String location) {
		List<BinaryLight> binaryLightsAtLocation = getBinaryLightsFromLocation(location);
		List<DimmerLight> dimmerLightsAtLocation = getDimmerLightsFromLocation(location);

		// count the number of lights
		int numberOfBinaryLightsTurnOn = getNumberOfBinaryLightsTurnOn(location);
		int numberOfDimmerLightsTurnOn = getNumberOfDimmerLightsTurnOn(location);

		int numberOfLightsTurnOn = numberOfBinaryLightsTurnOn + numberOfDimmerLightsTurnOn;
		/*
		double energyUsing;
		*/

		System.out.println("----------------------------print part------------------------------");

		System.out.println("In this location " + location + " we have");
		System.out.println("sensor " + presenceSensor.getSerialNumber());
		System.out.println("binarylights " + binaryLightsAtLocation.size());
		System.out.println("dimmerlights " + dimmerLightsAtLocation.size());
		System.out.println("with number of lights which have turned on : " + numberOfLightsTurnOn);
		System.out.println("The maximum number of lights are : " + maxLightsToTurnOnPerRoom);
		System.out.println("The maximum energy per room is : " + maximumEnergyConsumptionAllowedInARoom);

		List<Integer> assignmentOfLightsTurnOn = getAssignmentOfLightsTurnOn(location);

		int allDimmerLights = dimmerLightsAtLocation.size();

		if (binaryLightsAtLocation.size() > assignmentOfLightsTurnOn.get(0)
				&& numberOfBinaryLightsTurnOn < assignmentOfLightsTurnOn.get(0)) {
			for (DimmerLight dimLight : dimmerLightsAtLocation) {
				if (dimLight.getPowerLevel() == 0.0)
					continue;
				dimLight.setPowerLevel(0.0);
				numberOfLightsTurnOn = numberOfLightsTurnOn - 1;
				numberOfDimmerLightsTurnOn = numberOfDimmerLightsTurnOn - 1;
			}
		}

		if (presenceSensor.getSensedPresence()) {
			double maximumOfDimmerLightsTurnOnfinal;
			double maximumOfBinaryLightsTurnOnfinal;

			// define a value of dimmer light power
			double dimmerLightPower = 0.0;
			double dimmerLightPowerExpected;
			if (dimmerLightsAtLocation.isEmpty()) {
				dimmerLightPower = 0.0;
			} else {
				for(DimmerLight dimLight : dimmerLightsAtLocation) {
					if (dimLight.getPowerLevel() > 0.0) {
						dimmerLightPower = dimLight.getPowerLevel();
						break;
					}
				}
				
			}

			if (assignmentOfLightsTurnOn.get(0) > maxLightsToTurnOnPerRoom) {
				maximumOfBinaryLightsTurnOnfinal = maxLightsToTurnOnPerRoom;
			} else {
				maximumOfBinaryLightsTurnOnfinal = assignmentOfLightsTurnOn.get(0);
			}

			if (assignmentOfLightsTurnOn.get(1) == 0) {
				maximumOfDimmerLightsTurnOnfinal = 0;
			} else if (allDimmerLights > maxLightsToTurnOnPerRoom - assignmentOfLightsTurnOn.get(0)) {
				maximumOfDimmerLightsTurnOnfinal = maxLightsToTurnOnPerRoom - assignmentOfLightsTurnOn.get(0);
			} else {
				maximumOfDimmerLightsTurnOnfinal = allDimmerLights;
			}
			System.out.println("REQUIREMENT1: We need binary lights: " + maximumOfBinaryLightsTurnOnfinal
					+ " and dimmer lights(idealy): " + maximumOfDimmerLightsTurnOnfinal);
			System.out.println("STATE1: Now We have binary lights: " + numberOfBinaryLightsTurnOn
					+ " and dimmer lights: " + numberOfDimmerLightsTurnOn);
			System.out.println("REQUIREMENT2: And the assignment according to energy is like: "
					+ assignmentOfLightsTurnOn.get(0) * 100 + "W for binary lights ; "
					+ assignmentOfLightsTurnOn.get(1) * 100 + "W for dimmer lights!");
			System.out.println("STATE2: The dimmer light power is : " + dimmerLightPower * 100 + "W; ");

			if (maximumOfDimmerLightsTurnOnfinal != 0) {
				dimmerLightPowerExpected =  ((double)assignmentOfLightsTurnOn.get(1) / (double)maximumOfDimmerLightsTurnOnfinal);
			} else {
				dimmerLightPowerExpected = 0.0;
			}

			System.out.println("REQUIREMENT3: The assignment power of dimmer light should be "
					+ dimmerLightPowerExpected * 100 + "W!");
			if (numberOfBinaryLightsTurnOn < maximumOfBinaryLightsTurnOnfinal
					|| numberOfDimmerLightsTurnOn < maximumOfDimmerLightsTurnOnfinal
					|| dimmerLightPower != dimmerLightPowerExpected) {
				System.out.println("So, we should turn on more lights or change the lights arrangement.");
				for (BinaryLight binLight : binaryLightsAtLocation) {
					if (numberOfBinaryLightsTurnOn < maximumOfBinaryLightsTurnOnfinal/* && numberOfLightsTurnOn < maxLightsToTurnOnPerRoom*/) {
						if (binLight.getPowerStatus())
							continue;
						binLight.turnOn();
						numberOfLightsTurnOn = numberOfLightsTurnOn + 1;
						numberOfBinaryLightsTurnOn = numberOfBinaryLightsTurnOn + 1;
						System.out.println("with number of lights is :" + numberOfLightsTurnOn);
					} else if (numberOfBinaryLightsTurnOn == maximumOfBinaryLightsTurnOnfinal) {
						continue;
					} else {
						if (binLight.getPowerStatus()) {
							binLight.turnOff();
							numberOfLightsTurnOn = numberOfLightsTurnOn - 1;
							numberOfBinaryLightsTurnOn = numberOfBinaryLightsTurnOn - 1;
						}
						System.out.println("with number of lights is :" + numberOfLightsTurnOn);
					}
				}
				// count the number of dimmer light
				for (DimmerLight dimLight : dimmerLightsAtLocation) {

					if (numberOfDimmerLightsTurnOn < maximumOfDimmerLightsTurnOnfinal) {

						System.out.println("######################## Need dimmer light######################");
						System.out.println("all number of dimmer lights :" + allDimmerLights
								+ "; dimmer lights have turned on: " + numberOfDimmerLightsTurnOn);

						//System.out.println("powerlevel is " + assignmentOfLightsTurnOn.get(1)/maximumOfDimmerLightsTurnOnfinal + "because of the rest assignment light number is :" + assignmentOfLightsTurnOn.get(1));
						if (dimLight.getPowerLevel() > 0.0) {
							dimLight.setPowerLevel(
									(double) assignmentOfLightsTurnOn.get(1) / maximumOfDimmerLightsTurnOnfinal);
							continue;
						}
						dimLight.setPowerLevel(
								(double) assignmentOfLightsTurnOn.get(1) / maximumOfDimmerLightsTurnOnfinal);
						numberOfLightsTurnOn = numberOfLightsTurnOn + 1;
						numberOfDimmerLightsTurnOn = numberOfDimmerLightsTurnOn + 1;
						System.out.println("with number of lights is :" + numberOfLightsTurnOn);
					} else if (numberOfDimmerLightsTurnOn == maximumOfDimmerLightsTurnOnfinal) {
						if (maximumOfDimmerLightsTurnOnfinal == 0)
							continue;
						dimLight.setPowerLevel(
								(double) assignmentOfLightsTurnOn.get(1) / maximumOfDimmerLightsTurnOnfinal);
						//continue;
					} else {
						if (dimLight.getPowerLevel() > 0.0) {
							dimLight.setPowerLevel(0.0);
							numberOfLightsTurnOn = numberOfLightsTurnOn - 1;
							numberOfDimmerLightsTurnOn = numberOfDimmerLightsTurnOn - 1;
						}
						System.out.println("with number of lights is :" + numberOfLightsTurnOn);
					}
				}
			} else {
				System.out.println("We don't need to turn on more lights.");
			}

		} else {
			System.out.println("However, we need to turn off all lights because the sensor is off.");
			// turn off all the lights
			for (BinaryLight binLight : binaryLightsAtLocation) {
				binLight.turnOff();
			}
			// count the number of dimmer light
			for (DimmerLight dimLight : dimmerLightsAtLocation) {
				dimLight.setPowerLevel(0.0);
			}
		}

		System.out.println("----------------------------end print part---------------------------");

	}

	public synchronized double illuminancePerRoom(String location) {
		List<DimmerLight> dimmerLightsAtLocation = getDimmerLightsFromLocation(location);
		double illuminancePerRoom = 0.0;
		for (DimmerLight dimLight : dimmerLightsAtLocation) {
			illuminancePerRoom += dimLight.getPowerLevel() * dimLight.getMaxPowerLevel() * ONE_WATT_TO_ONE_LUMEN;
		}
		System.out.println("The room of " + location + " has the illuminance : " + illuminancePerRoom
				+ "lumens.(Assuming that the area is unity)");
		return illuminancePerRoom;

	}

	private synchronized List<Integer> getAssignmentOfLightsTurnOn(String location) {
		List<Integer> assignmentLightsNumbers = new ArrayList<Integer>();
		int maximumNumberOfLightsByEnergy = (int) (maximumEnergyConsumptionAllowedInARoom / 100);
		List<BinaryLight> binaryLightsAtLocation = getBinaryLightsFromLocation(location);
		List<DimmerLight> dimmerLightsAtLocation = getDimmerLightsFromLocation(location);
		int NumberOfBinaryLights = binaryLightsAtLocation.size();
		int NumberOfDimmerLights = dimmerLightsAtLocation.size();

		if (NumberOfBinaryLights >= maximumNumberOfLightsByEnergy) {
			assignmentLightsNumbers.clear();
			assignmentLightsNumbers.add(maximumNumberOfLightsByEnergy);
			assignmentLightsNumbers.add(0);
		} else {
			assignmentLightsNumbers.clear();
			assignmentLightsNumbers.add(NumberOfBinaryLights);
			assignmentLightsNumbers.add(maximumNumberOfLightsByEnergy - NumberOfBinaryLights);
		}
		return assignmentLightsNumbers;

	}

	// count the number of lights which turn on at the location specific
	/*
	private synchronized int getNumberOfLightsTurnOn(String location) {
		List<BinaryLight> binaryLightsAtLocation = getBinaryLightsFromLocation(location);
		List<DimmerLight> dimmerLightsAtLocation = getDimmerLightsFromLocation(location);
		int numberOfLightsTurnOn = 0;
		for (BinaryLight binLight : binaryLightsAtLocation) {
			if (binLight.getPowerStatus()) {
				numberOfLightsTurnOn++;
			}
		}
		for (DimmerLight dimLight : dimmerLightsAtLocation) {
			if (dimLight.getPowerLevel() > 0.0) {
				numberOfLightsTurnOn++;
			}
		}
		return numberOfLightsTurnOn;
	}*/

	private synchronized int getNumberOfBinaryLightsTurnOn(String location) {
		List<BinaryLight> binaryLightsAtLocation = getBinaryLightsFromLocation(location);
		int numberOfLightsTurnOn = 0;
		for (BinaryLight binLight : binaryLightsAtLocation) {
			if (binLight.getPowerStatus()) {
				numberOfLightsTurnOn++;
			}
		}
		return numberOfLightsTurnOn;
	}

	private synchronized int getNumberOfDimmerLightsTurnOn(String location) {
		List<DimmerLight> dimmerLightsAtLocation = getDimmerLightsFromLocation(location);
		int numberOfLightsTurnOn = 0;
		for (DimmerLight dimLight : dimmerLightsAtLocation) {
			if (dimLight.getPowerLevel() > 0.0) {
				numberOfLightsTurnOn++;
			}
		}
		return numberOfLightsTurnOn;
	}

	@Override
	public void deviceAdded(GenericDevice arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deviceEvent(GenericDevice arg0, Object arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void devicePropertyAdded(GenericDevice arg0, String arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void devicePropertyRemoved(GenericDevice arg0, String arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deviceRemoved(GenericDevice arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public synchronized int getMaximumNumberOfLightsToTurnOn() {
		// TODO Auto-generated method stub
		return maxLightsToTurnOnPerRoom;
	}

	@Override
	public synchronized void setMaximumNumberOfLightsToTurnOn(int maximumNumberOfLightsToTurnOn) {
		// TODO Auto-generated method stub
		maxLightsToTurnOnPerRoom = maximumNumberOfLightsToTurnOn;

	}

	@Override
	public synchronized double getMaximumAllowedEnergyInRoom() {
		// TODO Auto-generated method stub
		return maximumEnergyConsumptionAllowedInARoom;
	}

	@Override
	public synchronized void setMaximumAllowedEnergyInRoom(double maximumEnergy) {
		// TODO Auto-generated method stub
		maximumEnergyConsumptionAllowedInARoom = maximumEnergy;

	}

	@Override
	public synchronized double getTargetedIlluminance() {
		// TODO Auto-generated method stub
		return targetedIlluminance;
	}

	@Override
	public synchronized void setTargetedIlluminance(double illuminance) {
		// TODO Auto-generated method stub
		targetedIlluminance = illuminance;

	}

}
