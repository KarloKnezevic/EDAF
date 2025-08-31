package com.knezevic.edaf.testing.bbob.noiseless;

import com.knezevic.edaf.testing.bbob.Benchmark;

/**
 * Noisless Functions Factory. Produces benchmark for received benchmark id,
 * instance id and dimension.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class NoislessFunctionsFactory {

	/**
	 * Produces benchmark for received benchmark id, instance id and dimension.
	 * 
	 * @param benchmarkId
	 *            benchmark id
	 * @param instanceId
	 *            instance id; used to change the function optimum
	 * @param dimension
	 *            domain vector dimension
	 * @return
	 */
	public static Benchmark getBenchmark(int benchmarkId, int instanceId,
			int dimension) {

		switch (benchmarkId) {
		case 1:
			return new F1(instanceId, dimension);
		case 2:
			return new F2(instanceId, dimension);
		case 3:
			return new F3(instanceId, dimension);
		case 4:
			return new F4(instanceId, dimension);
		case 5:
			return new F5(instanceId, dimension);
		case 6:
			return new F6(instanceId, dimension);
		case 7:
			return new F7(instanceId, dimension);
		case 8:
			return new F8(instanceId, dimension);
		case 9:
			return new F9(instanceId, dimension);
		case 10:
			return new F10(instanceId, dimension);
		case 11:
			return new F11(instanceId, dimension);
		case 12:
			return new F12(instanceId, dimension);
		case 13:
			return new F13(instanceId, dimension);
		case 14:
			return new F14(instanceId, dimension);
		case 15:
			return new F15(instanceId, dimension);
		case 16:
			return new F16(instanceId, dimension);
		case 17:
			return new F17(instanceId, dimension);
		case 18:
			return new F18(instanceId, dimension);
		case 19:
			return new F19(instanceId, dimension);
		case 20:
			return new F20(instanceId, dimension);
		case 21:
			return new F21(instanceId, dimension);
		case 22:
			return new F22(instanceId, dimension);
		case 23:
			return new F23(instanceId, dimension);
		case 24:
			return new F24(instanceId, dimension);
		}

		return null;

	}

}
