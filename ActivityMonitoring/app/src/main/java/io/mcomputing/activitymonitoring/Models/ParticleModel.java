package io.mcomputing.activitymonitoring.Models;

import android.location.Location;

import java.util.Random;

import io.mcomputing.activitymonitoring.Helpers.ParticleHelper;


public class ParticleModel {

	public float forwardNoise, turnNoise, senseNoise;
	public float x, y, orientation;
	public int worldWidth;
	public int worldHeight;
	public double probability = 0;
	public Location[] landmarks;
	Random random;

	/**
	 * Default constructor for a particle
	 *
	 * @param landmarks Point array of landmark points for the particle
	 * @param width width of the particle's world in pixels
	 * @param height height of the particle's world in pixels
	 */
	public ParticleModel(Location[] landmarks, int width, int height) {
		this.landmarks = landmarks;
		worldHeight = height;
		worldWidth = width;
		random = new Random();
		x = random.nextFloat() * width;
		y = random.nextFloat() * height;
		orientation = random.nextFloat() * 2f * ((float)Math.PI);
		forwardNoise = 0f;
		turnNoise = 0f;
		senseNoise = 0f;
	}

	public void setNoise(float Fnoise, float Tnoise, float Snoise) {
		this.forwardNoise = Fnoise;
		this.turnNoise = Tnoise;
		this.senseNoise = Snoise;
	}

	public void set(float x, float y, float orientation, double prob) throws Exception {
		if(x < (0 - worldWidth) || x >= worldWidth) {
			throw new Exception("Latitude out of bounds");
		}
		if(y < (0 - worldHeight) || y >= worldHeight) {
			throw new Exception("Longitude out of bounds");
		}
		if(orientation < 0 || orientation >= 2 * Math.PI) {
			throw new Exception("Orientation out of bounds");
		}
		this.x = x;
		this.y = y;
		this.orientation = orientation;
		this.probability = prob;
	}

	public float[] sense() {
		float[] ret = new float[landmarks.length];

		for(int i=0;i<landmarks.length;i++){
			float dist = (float) ParticleHelper.distance(x, y,
					(float) landmarks[i].getLatitude(),
					(float) landmarks[i].getLongitude());
			ret[i] = dist + (float)random.nextGaussian() * senseNoise;
		}
		return ret;
	}

	public void move(float turn, float forward) throws Exception {
		if(forward < 0) {
			throw new Exception("Robot cannot move backwards");
		}
		orientation = orientation + turn + (float)random.nextGaussian() * turnNoise;
		orientation = circle(orientation, 2f * (float)Math.PI);

		double dist = forward + random.nextGaussian() * forwardNoise;

		x += Math.cos(orientation) * dist;
		y += Math.sin(orientation) * dist;
		x = circle(x, worldWidth);
		y = circle(y, worldHeight);
	}

	/**
	 * Calculates the probability of particle based on another particle's sense()
	 *
	 * @param measurement distance measurements from another particle's sense()
	 * @return the probability of the particle being correct, between 0 and 1
	 */
	public double measurementProb(float[] measurement) {
		double prob = 1.0;
		for(int i=0;i<landmarks.length;i++) {
			float dist = (float) ParticleHelper.distance(x, y,
					(float) landmarks[i].getLatitude(),
					(float) landmarks[i].getLongitude());
			prob *= ParticleHelper.Gaussian(dist, senseNoise, measurement[i]);
		}

		probability = prob;

		return prob;
	}

	private float circle(float num, float length) {
		while(num > length - 1) num -= length;
		while(num < 0) num += length;
		return num;
	}

	@Override
	public String toString() {
		return "[x=" + x + " y=" + y + " orient=" + Math.toDegrees(orientation) + " prob=" +probability +  "]";
	}

}
